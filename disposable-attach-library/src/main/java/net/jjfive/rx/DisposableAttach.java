package net.jjfive.rx;

import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.functions.ObjectHelper;
import net.jjfive.rx.internal.*;

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
        return new AttachDisposableObservable<>(upstream, this.compositeDisposable);
    }

    @Override
    public Flowable<T> apply(Flowable<T> upstream) {
        return new AttachDisposableFlowable<>(upstream, this.compositeDisposable);
    }

    @Override
    public SingleSource<T> apply(@NonNull Single<T> upstream) {
        return new AttachDisposableSingle<>(upstream, this.compositeDisposable);
    }

    @Override
    public MaybeSource<T> apply(@NonNull Maybe<T> upstream) {
        return new AttachDisposableMaybe<>(upstream, this.compositeDisposable);
    }

    @Override
    public CompletableSource apply(@NonNull Completable upstream) {
        return new AttachDisposableCompletable(upstream, this.compositeDisposable);
    }
}
