package net.jjfive.rx.internal;


import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.fuseable.HasUpstreamObservableSource;
import io.reactivex.plugins.RxJavaPlugins;

public class AttachDisposableObservable<T> extends Observable<T> implements HasUpstreamObservableSource<T> {

    protected final CompositeDisposable compositeDisposable;

    /**
     * The source consumable Observable.
     */
    protected final ObservableSource<T> source;

    /**
     *
     * @param source the consumable Observable
     * @param compositeDisposable the composite disposable
     */
    public AttachDisposableObservable(ObservableSource<T> source, CompositeDisposable compositeDisposable) {
        this.source = source;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public final ObservableSource<T> source() {
        return source;
    }

    @Override
    public void subscribeActual(Observer<? super T> s) {
        Observer<? super T> observer;
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

        source.subscribe(new AttachSingleObserver<>(observer, this.compositeDisposable));
    }

    static final class AttachSingleObserver<T>  implements Observer<T>, Disposable {
        final Observer<? super T> actual;
        final CompositeDisposable compositeDisposable;

        Disposable s;

        public AttachSingleObserver(Observer<? super T> actual, CompositeDisposable compositeDisposable) {
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
        public void onNext(T t) {
            actual.onNext(t);
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
        public void onComplete() {
            if (s != DisposableHelper.DISPOSED) {
                actual.onComplete();
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
