package com.hino.rxjavaplayground;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.myLabel) TextView textView;

    private Observable<Integer> observable;
    private Disposable disposable;
    private DisposableObserver<Integer> disposableObserver;
    private int sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Create an observable
        observable = Observable.create(emitter -> {
            for (int i = 1; i <= 200; i++) {
                SystemClock.sleep(100l); // simulate delay
                emitter.onNext(i);
            }
            emitter.onComplete();
        });
    }

    @OnClick(R.id.subscribeButton)
    public void onButtonClick() {
        Toast.makeText(this, "Button does nothing...", Toast.LENGTH_SHORT);

        // create a disposable subscriber
        //new Thread(() -> createNewDisposableObserver()).start();

        // create disposable
        new Thread(() -> createNewDisposable()).start();
    }

    private void createNewDisposable() {
        disposable = observable
                .observeOn(AndroidSchedulers.mainThread()) // subscriber observes on the main thread
                .subscribeOn(Schedulers.computation()) // observable is called outside the main thread
                .subscribe(t -> {
                    Timber.d("onNext(): %d", t);
                    sum += t;
                    textView.setText(String.format("%d", t));
                });
    }

    private void createNewDisposableObserver() {
        disposableObserver = observable.subscribeWith(
                new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        Timber.d(String.format("onNext(): %d", integer));
                        sum += integer;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e.getMessage(), e);
                    }

                    @Override
                    public void onComplete() {
                        Timber.i("Completed with sum: %d", sum);
                        new Handler(Looper.getMainLooper()).post(() -> textView.setText(String.format("%d", sum)));
                    }
                });
    }

    @OnClick(R.id.unsubscribeButton)
    public void onUnsubscribeClick() {
        Timber.i("You have unsubscribed");
        if (disposableObserver != null) {
            disposableObserver.dispose();
        }

        if (disposable != null) {
            disposable.dispose();
        }

        textView.setText(String.format("%d", sum));
    }

}
