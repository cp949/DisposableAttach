/*
 * Copyright 2017 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jjfive.rx;
import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DisposableAttachObservableTest {
    @Test public void test() {
        PublishSubject<String> subject = PublishSubject.create();
        Observable<String> observerable = subject.hide();

        TestObserver testObserver = new TestObserver();
        CompositeDisposable composite = new CompositeDisposable();
        Disposable disposable = observerable
                .compose(DisposableAttach.<String>to(composite))
                .subscribeWith(testObserver);

        subject.onNext("Foo");
        testObserver.assertValue("Foo");
        assertTrue(composite.size() == 1);
        composite.dispose();
        assertTrue(composite.size() == 0);
        assertTrue(composite.isDisposed());
        assertTrue(disposable.isDisposed());
        assertTrue(testObserver.isDisposed());
    }
}
