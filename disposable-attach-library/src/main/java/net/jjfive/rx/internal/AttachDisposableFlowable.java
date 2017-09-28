package net.jjfive.rx.internal;


import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.fuseable.HasUpstreamPublisher;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subscribers.DisposableSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class AttachDisposableFlowable<T> extends Flowable<T> implements HasUpstreamPublisher<T> {

    protected final CompositeDisposable compositeDisposable;

    /**
     * The source consumable Publisher.
     */
    protected final Publisher<T> source;

    /**
     * @param source              the consumable Publisher
     * @param compositeDisposable the composite disposable
     */
    public AttachDisposableFlowable(Publisher<T> source, CompositeDisposable compositeDisposable) {
        this.source = source;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public final Publisher<T> source() {
        return source;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {

        Subscriber<? super T> subscriber;
        try {
            subscriber = ObjectHelper.requireNonNull(s, "Null Observer");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // can't call onError because no way to know if a Disposable has been set or not
            // can't call onSubscribe because the call might have set a Disposable already
            RxJavaPlugins.onError(e);

            NullPointerException npe = new NullPointerException("Actually not, but can't throw other exceptions due to RS");
            npe.initCause(e);
            throw npe;
        }

        source.subscribe(new AttachSingleObserver<>(subscriber, this.compositeDisposable));
    }

    static final class AttachSingleObserver<T> extends DisposableSubscriber<T> {
        final Subscriber<? super T> actual;
        final CompositeDisposable compositeDisposable;

        public AttachSingleObserver(Subscriber<? super T> actual, CompositeDisposable compositeDisposable) {
            this.actual = actual;
            this.compositeDisposable = compositeDisposable;
        }

        @Override
        protected void onStart() {
            if (actual instanceof Disposable) {
                this.compositeDisposable.add((Disposable) actual);
            } else {
                this.compositeDisposable.add(this);
            }
            super.onStart();
        }

        @Override
        public void onError(Throwable t) {
            if (isDisposed() == false) {
                actual.onError(t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }


        @Override
        public void onComplete() {
            if (isDisposed() == false) {
                actual.onComplete();
            }
        }

        @Override
        public void onNext(T t) {
            actual.onNext(t);
        }
    }
}
