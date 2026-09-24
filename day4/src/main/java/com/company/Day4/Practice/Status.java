package com.company.Day4.Practice;

import lombok.Getter;

public enum Status {
    NEW, PAID, SHIPPED,CANCELLED;
}
// я создал класс который создал внутри себя публичные статичные обьекты
// и каждый раз когда я указываю статус то у меня используется все время
// один и тот же обьект