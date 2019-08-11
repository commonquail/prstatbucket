package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Links {

    public final Link decline;
    public final Link commits;
    public final Link self;
    public final Link comments;
    public final Link merge;
    public final Link html;
    public final Link activity;
    public final Link diff;
    public final Link approve;
    public final Link statuses;

    public Links(
            @JsonProperty("decline") Link decline,
            @JsonProperty("commits") Link commits,
            @JsonProperty("self") Link self,
            @JsonProperty("comments") Link comments,
            @JsonProperty("merge") Link merge,
            @JsonProperty("html") Link html,
            @JsonProperty("activity") Link activity,
            @JsonProperty("diff") Link diff,
            @JsonProperty("approve") Link approve,
            @JsonProperty("statuses") Link statuses) {
        super();
        this.decline = decline;
        this.commits = commits;
        this.self = self;
        this.comments = comments;
        this.merge = merge;
        this.html = html;
        this.activity = activity;
        this.diff = diff;
        this.approve = approve;
        this.statuses = statuses;
    }
}
