package net.jjfive.rx.internal;


import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.fuseable.HasUpstreamMaybeSource;
import io.reactivex.plugins.RxJavaPlugins;

public class AttachDisposableMaybe<T> extends Maybe<T> implements HasUpstreamMaybeSource<T> {

    protected final CompositeDisposable compositeDisposable;

    /**
     * The source consumable MaybeSource.
     */
    protected final MaybeSource<T> source;

    /**
     *
     * @param source the consumable MaybeSource
     * @param compositeDisposable the composite disposable
     */
    public AttachDisposableMaybe(MaybeSource<T> source, CompositeDisposable compositeDisposable) {
        this.source = source;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public final MaybeSource<T> source() {
        return source;
    }

    @Override
    protected void subscribeActual(MaybeObserver<? super T> s) {
        MaybeObserver<? super T> observer;
        try {
            observer = ObjectHelper.requireNonNull(s, "Null Observer");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // can't call onError because no way to know if a Disposable has been set or not
            // can't call onSubscribe because the call might have set a Disposable already
            RxJavaPlugins.onError(e);

            NullPointerException npe = new NullPointerException("Actually not, but can't throw other exceptions due to RS");
            npe.initCause(e);
            throw npe;
        }

        source.subscribe(new AttachMaybeObserver<>(observer, this.compositeDisposable));
    }

    static final class AttachMaybeObserver<T>  implements MaybeObserver<T>, Disposable {
        final MaybeObserver<? super T> actual;
        final CompositeDisposable compositeDisposable;

        Disposable s;

        public AttachMaybeObserver(MaybeObserver<? super T> actual, CompositeDisposable compositeDisposable) {
            this.actual = actual;
            this.compositeDisposable = compositeDisposable;
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                actual.onSubscribe(this);
                if(actual instanceof Disposable) {
                    this.compositeDisposable.add((Disposable) actual);
                } else {
                    this.compositeDisposable.add(this);
                }
                this.s = s;                
            }
        }

        @Override
        public void onSuccess(T t) {
            actual.onSuccess(t);
        }

        @Override
        public void onComplete() {
            actual.onComplete();
        }

        @Override
        public void onError(Throwable t) {
            if (s != DisposableHelper.DISPOSED) {
                actual.onError(t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void dispose() {
            s.dispose();
        }

        @Override
        public boolean isDisposed() {
            return s.isDisposed();
        }
    }
}
