package com.leyou.auth.item.bo;

import com.leyou.auth.item.pojo.Sku;
import com.leyou.auth.item.pojo.Spu;
import com.leyou.auth.item.pojo.SpuDetail;

import java.util.List;

/**
 * 要注意，页面展示的事商品分类和品牌名称，而数据库中保存的是id，怎么办？
 * 我们乐意新建一个类，继承SPU，并且拓展cname和bname属性，写到leyou-item-interface
 */
public class SpuBo extends Spu {
    public SpuDetail getSpuDetail() {
        return spuDetail;
    }

    public void setSpuDetail(SpuDetail spuDetail) {
        this.spuDetail = spuDetail;
    }

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }

    String cname;   //商品分类名称

    String bname;   //品牌名称

    SpuDetail spuDetail;    //商品详情

    List<Sku>  skus;    //sku列表


    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    @Override
    public String toString() {
        return "SpuBo{" +
                "cname='" + cname + '\'' +
                ", bname='" + bname + '\'' +
                ", spuDetail=" + spuDetail +
                ", skus=" + skus +
                '}';
    }
}
