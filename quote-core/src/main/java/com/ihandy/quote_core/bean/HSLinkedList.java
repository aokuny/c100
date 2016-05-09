package com.ihandy.quote_core.bean;

import java.util.LinkedList;

/**
 * Created by fengwen on 2016/5/7.
 */
public class HSLinkedList<E1,E2>   {
    protected Node<E1,E2> head;//头指针，指向单链表的头结点
    protected Node<E1,E2> rear;//尾指针，指向单链表最后一个结点
    protected int n;//单链表长度

    /*
     * 构造空单链表
     */
    public HSLinkedList(){
        this.head = new Node<E1,E2>(null,null);//创建头结点
        this.n = 0;
    }



}
