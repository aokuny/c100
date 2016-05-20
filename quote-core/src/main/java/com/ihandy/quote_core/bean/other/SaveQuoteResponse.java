package com.ihandy.quote_core.bean.other;

/**
 * Created by fengwen on 2016/4/29.
 */
public class SaveQuoteResponse {

    private int Source;//来源 0：平安 1：太平洋 2：人保

    private double CheSun;//车损
    private double SanZhe;//三者
    private double DaoQiang;//盗抢
    private double SiJi;//司机
    private double ChengKe;//乘客

    private double Boli;//玻璃
    private double HuaHen;//划痕

    private double  BuJiMianCheSun;//不计免车损
    private double  BuJiMianSanZhe;//不计免三者
    private double  BuJiMianDaoQiang;//不计免盗抢
    private double  BuJiMianRenYuan;//不计免人员
    private double  BuJiMianFuJia;//不计免附加

    private double SheShui;//涉水
    private double CheDeng;//车灯
    private double ZiRan;//自燃


    private double getBizTotal()
    {
        return  (this.CheSun + this.SanZhe + this.DaoQiang + this.SiJi + this.ChengKe + this.Boli + this.HuaHen + this.BuJiMianCheSun + this.BuJiMianSanZhe + this.BuJiMianDaoQiang + this.BuJiMianRenYuan + this.BuJiMianFuJia  + this.SheShui + this.CheDeng + this.ZiRan);
    }

    public int getSource() {
        return Source;
    }

    public void setSource(int source) {
        Source = source;
    }

    public double getCheSun() {
        return CheSun;
    }

    public void setCheSun(double cheSun) {
        CheSun = cheSun;
    }

    public double getSanZhe() {
        return SanZhe;
    }

    public void setSanZhe(double sanZhe) {
        SanZhe = sanZhe;
    }

    public double getDaoQiang() {
        return DaoQiang;
    }

    public void setDaoQiang(double daoQiang) {
        DaoQiang = daoQiang;
    }

    public double getSiJi() {
        return SiJi;
    }

    public void setSiJi(double siJi) {
        SiJi = siJi;
    }

    public double getChengKe() {
        return ChengKe;
    }

    public void setChengKe(double chengKe) {
        ChengKe = chengKe;
    }

    public double getBoli() {
        return Boli;
    }

    public void setBoli(double boli) {
        Boli = boli;
    }

    public double getHuaHen() {
        return HuaHen;
    }

    public void setHuaHen(double huaHen) {
        HuaHen = huaHen;
    }

    public double getBuJiMianCheSun() {
        return BuJiMianCheSun;
    }

    public void setBuJiMianCheSun(double buJiMianCheSun) {
        BuJiMianCheSun = buJiMianCheSun;
    }

    public double getBuJiMianSanZhe() {
        return BuJiMianSanZhe;
    }

    public void setBuJiMianSanZhe(double buJiMianSanZhe) {
        BuJiMianSanZhe = buJiMianSanZhe;
    }

    public double getBuJiMianDaoQiang() {
        return BuJiMianDaoQiang;
    }

    public void setBuJiMianDaoQiang(double buJiMianDaoQiang) {
        BuJiMianDaoQiang = buJiMianDaoQiang;
    }

    public double getBuJiMianRenYuan() {
        return BuJiMianRenYuan;
    }

    public void setBuJiMianRenYuan(double buJiMianRenYuan) {
        BuJiMianRenYuan = buJiMianRenYuan;
    }

    public double getBuJiMianFuJia() {
        return BuJiMianFuJia;
    }

    public void setBuJiMianFuJia(double buJiMianFuJia) {
        BuJiMianFuJia = buJiMianFuJia;
    }

    public double getSheShui() {
        return SheShui;
    }

    public void setSheShui(double sheShui) {
        SheShui = sheShui;
    }

    public double getCheDeng() {
        return CheDeng;
    }

    public void setCheDeng(double cheDeng) {
        CheDeng = cheDeng;
    }

    public double getZiRan() {
        return ZiRan;
    }

    public void setZiRan(double ziRan) {
        ZiRan = ziRan;
    }
}
