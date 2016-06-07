package com.test.augment.model;

import java.io.Serializable;

public class Repo implements Serializable{
    long id;
    public String name;
    public String full_name;
    public RepoOwner owner;
    public boolean fork;
    public String description;
    public String html_url;
    public String url;
}
