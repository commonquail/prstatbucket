package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public final class PullRequest {

    public enum State {
        MERGED,
        SUPERSEDED,
        OPEN,
        DECLINED,
        @JsonEnumDefaultValue
        UNKNOWN;
    }

    public final String description;
    public final Links links;
    public final String title;
    public final boolean closeSourceBranch;
    public final String type;
    public final int id;
    public final Destination destination;
    public final Instant createdOn;
    public final Summary summary;
    public final Source source;
    public final int commentCount;
    public final State state;
    public final int taskCount;
    public final String reason;
    public final Instant updatedOn;
    public final Author author;
    public final MergeCommit mergeCommit;
    public final ClosedBy closedBy;

    /**
     * @param description
     * @param links
     * @param title
     * @param closeSourceBranch
     * @param type
     * @param id
     * @param destination
     * @param createdOn
     * @param summary
     * @param source
     * @param commentCount
     * @param state
     * @param taskCount
     * @param reason
     * @param updatedOn
     * @param author
     * @param mergeCommit
     * @param closedBy
     */
    public PullRequest(
            @JsonProperty("description") String description,
            @JsonProperty("links") Links links,
            @JsonProperty("title") String title,
            @JsonProperty("close_source_branch") boolean closeSourceBranch,
            @JsonProperty("type") String type,
            @JsonProperty("id") int id,
            @JsonProperty("destination") Destination destination,
            @JsonProperty("created_on") Instant createdOn,
            @JsonProperty("summary") Summary summary,
            @JsonProperty("source") Source source,
            @JsonProperty("comment_count") int commentCount,
            @JsonProperty("state") State state,
            @JsonProperty("task_count") int taskCount,
            @JsonProperty("reason") String reason,
            @JsonProperty("updated_on") Instant updatedOn,
            @JsonProperty("author") Author author,
            @JsonProperty("merge_commit") MergeCommit mergeCommit,
            @JsonProperty("closed_by") ClosedBy closedBy) {
        super();
        this.description = description;
        this.links = links;
        this.title = title;
        this.closeSourceBranch = closeSourceBranch;
        this.type = type;
        this.id = id;
        this.destination = destination;
        this.createdOn = createdOn;
        this.summary = summary;
        this.source = source;
        this.commentCount = commentCount;
        this.state = state;
        this.taskCount = taskCount;
        this.reason = reason;
        this.updatedOn = updatedOn;
        this.author = author;
        this.mergeCommit = mergeCommit;
        this.closedBy = closedBy;
    }
}
