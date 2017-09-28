package net.jjfive.rx.internal;


import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.fuseable.HasUpstreamSingleSource;
import io.reactivex.plugins.RxJavaPlugins;

public class AttachDisposableSingle<T> extends Single<T> implements HasUpstreamSingleSource<T> {

    protected final CompositeDisposable compositeDisposable;

    /**
     * The source consumable SingleSource.
     */
    protected final SingleSource<T> source;

    /**
     *
     * @param source the consumable SingleSource
     * @param compositeDisposable the composite disposable
     */
    public AttachDisposableSingle(SingleSource<T> source, CompositeDisposable compositeDisposable) {
        this.source = source;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public final SingleSource<T> source() {
        return source;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super T> s) {
        SingleObserver<? super T> observer;
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

    static final class AttachSingleObserver<T>  implements SingleObserver<T>, Disposable {
        final SingleObserver<? super T> actual;
        final CompositeDisposable compositeDisposable;

        Disposable s;

        public AttachSingleObserver(SingleObserver<? super T> actual, CompositeDisposable compositeDisposable) {
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
                    this.compositeDisposable.add(s);
                }
                this.s = s;                
            }
        }

        @Override
        public void onSuccess(T t) {
            actual.onSuccess(t);
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
