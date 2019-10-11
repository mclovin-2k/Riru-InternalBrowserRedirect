package com.github.kr328.ibr.remote.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RuleSet implements Parcelable {
    public static final Creator<RuleSet> CREATOR = new Creator<RuleSet>() {
        @Override
        public RuleSet createFromParcel(Parcel in) {
            return new RuleSet(in);
        }

        @Override
        public RuleSet[] newArray(int size) {
            return new RuleSet[size];
        }
    };
    private String tag;
    private List<String> extras;
    private List<Rule> rules;

    public RuleSet() {
        tag = "";
        extras = new ArrayList<>();
        rules = new ArrayList<>();
    }

    private RuleSet(Parcel in) {
        tag = in.readString();
        extras = new ArrayList<>();
        in.readStringList(extras);
        rules = in.createTypedArrayList(Rule.CREATOR);
    }

    public static RuleSet readFromJson(JSONObject jsonObject) throws JSONException {
        RuleSet result = new RuleSet();

        result.tag = jsonObject.getString("tag");

        JSONArray array = jsonObject.getJSONArray("extras");
        for (int i = 0; i < array.length(); i++)
            result.extras.add(array.getString(i));

        array = jsonObject.getJSONArray("rules");
        for (int i = 0; i < array.length(); i++)
            result.rules.add(Rule.parseFromJson(array.getJSONObject(i)));

        return result;
    }

    @Override
    public int describeContents() {
        return "StoreRuleSet".hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(tag);
        parcel.writeStringList(extras);
        parcel.writeTypedList(rules);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject result = new JSONObject();

        JSONArray array = new JSONArray();
        for (Rule rule : rules)
            array.put(rule.toJson());

        result.put("tag", tag);
        result.put("rules", array);
        result.put("extras", new JSONArray(extras));

        return result;
    }

    public List<String> getExtras() {
        return extras;
    }

    public void setExtras(List<String> extras) {
        this.extras = extras;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
