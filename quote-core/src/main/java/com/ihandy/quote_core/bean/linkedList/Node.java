package com.ihandy.quote_core.bean.linkedList;

/**
 * Created by fengwen on 2016/5/7.
 */
public class Node<E1,E2> {

    public E1 id;//数据域
    public E2 data;//
    public Node<E1,E2> next;//地址域

    /*
     * 构造结点
     */
    public Node(E1 id,E2 data,Node<E1,E2> next){
        this.id = id;
        this.data = data;
        this.next = next;
    }
    public Node(E1 id, E2 data){
        this(id,data,null);
    }
    public Node(){
        this(null,null,null);
    }



}

