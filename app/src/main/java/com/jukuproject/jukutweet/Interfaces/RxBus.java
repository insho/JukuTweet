package com.jukuproject.jukutweet.Interfaces;


import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Creates Observable objects to catch click events from an adapter row, and act on them
 * in a fragment activity
 */
public class RxBus {

    private final Subject<Object, Object> _busClick = new SerializedSubject<>(PublishSubject.create());
    private final Subject<Object, Object> _busLongClick = new SerializedSubject<>(PublishSubject.create());
    private final Subject<Object, Object> _busDeselectClick = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {
        _busClick.onNext(o);
    }
    public Observable<Object> toClickObserverable() {
        return _busClick;
    }


    public void sendLongClick(Object o) {
        _busLongClick.onNext(o);
    }
    public Observable<Object> toLongClickObserverable() {
        return _busLongClick;
    }


    public void sendDeselect(Object o) {
        _busDeselectClick.onNext(o);
    }
    public Observable<Object> toDeselectClickObservable() {
        return _busDeselectClick;
    }


}