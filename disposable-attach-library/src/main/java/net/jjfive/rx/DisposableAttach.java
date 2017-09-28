package net.jjfive.rx;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.subscribers.DisposableSubscriber;
import org.reactivestreams.Subscriber;

public final class DisposableAttach<T>
    implements ObservableTransformer<T, T>, FlowableTransformer<T, T>, SingleTransformer<T, T>,
    MaybeTransformer<T, T>, CompletableTransformer {

    public static <T> DisposableAttach<T> to(CompositeDisposable compositeDisposable) {
        return new DisposableAttach(compositeDisposable);
    }

    private final CompositeDisposable compositeDisposable;

    private DisposableAttach(CompositeDisposable compositeDisposable) {
        ObjectHelper.requireNonNull(compositeDisposable, "compositeDisposable must be not null");
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public Observable<T> apply(Observable<T> upstream) {
        return upstream.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                compositeDisposable.add(disposable);
            }
        });
    }

    @Override
    public Flowable<T> apply(Flowable<T> upstream) {
        return new SimpleFlowable<>(upstream, this.compositeDisposable);
    }

    @Override
    public SingleSource<T> apply(@NonNull Single<T> upstream) {
        return upstream.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                compositeDisposable.add(disposable);
            }
        });
    }

    @Override
    public MaybeSource<T> apply(@NonNull Maybe<T> upstream) {
        return upstream.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                compositeDisposable.add(disposable);
            }
        });
    }

    @Override
    public CompletableSource apply(@NonNull Completable upstream) {
        return upstream.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                compositeDisposable.add(disposable);
            }
        });
    }

    static final class SimpleFlowable<T> extends Flowable<T> {
        private final Flowable<T> upstream;
        private final CompositeDisposable compositeDisposable;

        SimpleFlowable(Flowable<T> upstream, CompositeDisposable compositeDisposable) {
            this.upstream = upstream;
            this.compositeDisposable = compositeDisposable;
        }

        @Override
        protected void subscribeActual(Subscriber<? super T> subscriber) {
            SimpleDisposableSubscriber<T> disposableSubscriber =
                new SimpleDisposableSubscriber<T>(subscriber);
            compositeDisposable.add(disposableSubscriber);
            upstream.subscribe(disposableSubscriber);
        }
    }

    static final class SimpleDisposableSubscriber<T> extends DisposableSubscriber<T> {
        private final Subscriber<? super T> downstream;

        SimpleDisposableSubscriber(Subscriber<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onNext(T value) {
            downstream.onNext(value);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }
    }
}
