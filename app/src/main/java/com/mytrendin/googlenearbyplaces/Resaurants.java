package com.mytrendin.googlenearbyplaces;

/**
 * Created by BrainPlow on 8/25/2017.
 */

public class Resaurants {

    public String restId;
    public String restname;
    public String restamount;
    public String date;

    public Resaurants()
    {

    }
    public String getRestId() {
        return restId;
    }

    public String getRestname() {
        return restname;
    }

    public String getRestamount() {
        return restamount;
    }

    public String getDate() {
        return date;
    }



    public Resaurants(String restId, String restname, String restamount, String date) {
        this.date = date;
        this.restId = restId;
        this.restname = restname;
        this.restamount = restamount;
    }


}
