package com.ihandy.quote_core.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by fengwen on 2016/5/7.
 */
public class SortedHSLinkedList<E1,E2>{
    protected Node<E1,E2> head;//头指针，指向单链表的头结点
    protected Node<E1,E2> rear;//尾指针，指向单链表最后一个结点
    protected int n;//单链表长度


    public SortedHSLinkedList() {
        this.head = new Node<E1,E2>(null,null);//创建头结点
        this.n = 0;
    }

    /*
     * 根据指定对象的大小，插入到指定的位置
     */
    public boolean insertNode(E1 element1, E2 element2) {
        if (element1 == null || !(element1 instanceof Comparable)) {
            return false;//不能插入null或者非comparable对象
        }
        Comparable cmp = (Comparable) element1;
        Node<E1, E2> front = this.head, p = front.next;//front是p的前驱结点
        while (p != null && cmp.compareTo(p.id) > 0) {//比较大小，如果要插入的元素大于p值
            front = p;
            p = p.next;//往后行动front和p指针一次
        }
        front.next = new Node<E1, E2>(element1, element2, p);//插入，把新插入的结点的引用赋值给front的域引用
        if (p == null) {
            this.rear = front.next;//尾插入，修改尾指针
        }
        this.n++;
        return true;
    }

    //获取node
    public Node<E1, E2> getNodeById(int nodeId) {
        Node<E1, E2> node=null;

        Node<E1, E2> front = this.head, p = front.next;//front是p的前驱结点
        while (p != null && (Integer) p.id == nodeId) {//比较大小，如果要插入的元素大于p值
            node = p;
            break;
        }
        return node;
    }

    //遍历链表
    public void printAll(){
        Node postion=this.head;
        while(postion!=null){
            System.out.println("nodeId = "+ postion.id+ " nodeUrl = " + postion.data+"\n");
            postion = postion.next;
        }
    }

    public static void main(String[] args) {
        SortedHSLinkedList<Integer,String> list = new SortedHSLinkedList<Integer,String>();
        int n = 10;
        System.out.println("insert: ");
        for(int i=0;i<n;i++){
            int k = (int)(Math.random()*100);
            list.insertNode(new Integer(k),new Integer(k)+"A");
            System.out.print(k+" ");
        }
        list.printAll();
       // System.out.println("\nlist:"+list.toString());
    }

    public Node<E1, E2> getHead() {
        return head;
    }

    public void setHead(Node<E1, E2> head) {
        this.head = head;
    }
}
