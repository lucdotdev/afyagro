package com.example.afyagro.models;

/**
 * A short lived photo update (a "story") shared by a farmer or a buyer.
 * Stories work like the ones on Instagram or TikTok: a publisher shares a
 * photo with an optional caption and it stays visible for a while
 * ({@link #STORY_LIFESPAN_MS}) before it is considered expired.
 */
public class Story {

    /** A story is considered active for 24 hours after it was published. */
    public static final long STORY_LIFESPAN_MS = 24L * 60L * 60L * 1000L;

    private String publisherId;
    private String publisherName;
    private String publisherPhoto;
    private String photoPath;
    private String caption;
    private long timestamp;
    private String uid;

    public Story() {
    }

    public Story(String publisherId, String publisherName, String publisherPhoto,
                 String photoPath, String caption, long timestamp) {
        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.publisherPhoto = publisherPhoto;
        this.photoPath = photoPath;
        this.caption = caption;
        this.timestamp = timestamp;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherPhoto() {
        return publisherPhoto;
    }

    public void setPublisherPhoto(String publisherPhoto) {
        this.publisherPhoto = publisherPhoto;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    /** @return true when the story is still within its {@link #STORY_LIFESPAN_MS}. */
    public boolean isActive() {
        return System.currentTimeMillis() - timestamp < STORY_LIFESPAN_MS;
    }
}
