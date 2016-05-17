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
}
