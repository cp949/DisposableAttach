RxJava Disposable Attach
======================

`DisposableAttach` is an RxJava 2 transformer which add disposable to CompositeDisposable operators.



Apply with `compose` to an upstream `Observable` or `Flowable` or `Single` or `Maybe` `Completable` for
all new subscribers.

```java
public class SampleActivity extends Activity {
    // ...
    CompositeDisposable mCompositeDisposable;
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mCompositeDisposable = new CompositeDisposable();
        
        // Below styles I used for many months and is bad
        
        // Style-1
        // The code works but it's easy to make mistake
        Disposable disposable = Observable.interval(1, TimeUnit.SECONDS)
                                          .subscribe(v -> System.out.println("" + v));
        mCompositeDisposable.add(disposable);
        
        
        // Style-2
        // The code works but it's ugly and confusing
        mCompositeDisposable.add(
                Observable.interval(1, TimeUnit.SECONDS)
                          .subscribe(v -> System.out.println("" + v));
        );
        
        
        
        // New styles with DisposableAttach, more simple
        Observable
            .interval(1, TimeUnit.SECONDS)
            .compose(DisposableAttach.to(mCompositeDisposable))
            .subscribe(v -> System.out.println("" + v));
            
        Observable
            .create(someDataSource())
            .compose(DisposableAttach.to(mCompositeDisposable))
            .subscribe(v -> System.out.println("" + v));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCompositeDisposable.dispose();
        mCompositeDisposable = null;
    }
}
```

Limitation
======================

`.compose(DisposableAttach.to(xx))`  should be placed directly above `subscribe()`.

Below code work properly.  `.compose(DisposableAttach.to(xx))` is directly above `.subscribe( ... )` 
```java
        // This is ok
        Observable
            .interval(1, TimeUnit.SECONDS)
            .map( v -> v + 1)
            .observeOn(Schedulers.io())
            .compose(DisposableAttach.to(mCompositeDisposable))
            .subscribe(v -> System.out.println("" + v));
```

But, below code is not work properly.  `.compose(DisposableAttach.to(xx))` is not directly above `.subscribe( ... )`
```java
        // This is not work properly
        Observable
            .interval(1, TimeUnit.SECONDS)
            .map( v -> v + 1)
            .compose(DisposableAttach.to(mCompositeDisposable))
            .observeOn(Schedulers.io())            
            .subscribe(v -> System.out.println("" + v));


```


Download
--------

Maven:
```xml
<dependency>
  <groupId>net.jjfive.rx</groupId>
  <artifactId>disposable-attach</artifactId>
  <version>0.0.4</version>
</dependency>
```
Gradle:
```groovy
compile 'net.jjfive.rx:disposable-attach:0.0.4'
```




License
-------

    Copyright 2017 jjfive

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



