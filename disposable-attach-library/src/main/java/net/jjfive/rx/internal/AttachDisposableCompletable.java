package net.jjfive.rx.internal;


import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.fuseable.HasUpstreamCompletableSource;
import io.reactivex.plugins.RxJavaPlugins;

public class AttachDisposableCompletable<T> extends Completable implements HasUpstreamCompletableSource {

    protected final CompositeDisposable compositeDisposable;

    /**
     * The source consumable CompletableSource.
     */
    protected final CompletableSource source;

    /**
     * @param source              the consumable CompletableSource
     * @param compositeDisposable the composite disposable
     */
    public AttachDisposableCompletable(CompletableSource source, CompositeDisposable compositeDisposable) {
        this.source = source;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public final CompletableSource source() {
        return source;
    }

    @Override
    protected void subscribeActual(CompletableObserver s) {
        CompletableObserver observer;
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

        source.subscribe(new AttachCompletableObserver(observer, this.compositeDisposable));
    }

    static final class AttachCompletableObserver implements CompletableObserver, Disposable {
        final CompletableObserver actual;
        final CompositeDisposable compositeDisposable;

        Disposable s;

        public AttachCompletableObserver(CompletableObserver actual, CompositeDisposable compositeDisposable) {
            this.actual = actual;
            this.compositeDisposable = compositeDisposable;
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                actual.onSubscribe(this);
                if (actual instanceof Disposable) {
                    this.compositeDisposable.add((Disposable) actual);
                } else {
                    this.compositeDisposable.add(this);
                }
                this.s = s;
            }
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
