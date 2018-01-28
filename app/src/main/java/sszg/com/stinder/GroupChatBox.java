package sszg.com.stinder;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupChatBox implements Parcelable {

    private String dateTime;
    private String image;
    private String groupName;
    private String roomNumber;

    private String additionalInfo;
    private String className;
    private String longitude;

    public GroupChatBox(String dateTime, String image, String groupName, String roomNumber, String additionalInfo, String className, String longitude, String latitude) {
        this.dateTime = dateTime;
        this.image = image;
        this.groupName = groupName;
        this.roomNumber = roomNumber;
        this.additionalInfo = additionalInfo;
        this.className = className;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    protected GroupChatBox(Parcel in) {
        dateTime = in.readString();
        image = in.readString();
        groupName = in.readString();
        roomNumber = in.readString();
        additionalInfo = in.readString();
        className = in.readString();
        longitude = in.readString();
        latitude = in.readString();
    }

    public static final Creator<GroupChatBox> CREATOR = new Creator<GroupChatBox>() {
        @Override
        public GroupChatBox createFromParcel(Parcel in) {
            return new GroupChatBox(in);
        }

        @Override
        public GroupChatBox[] newArray(int size) {
            return new GroupChatBox[size];
        }
    };

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    private String latitude;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dateTime);
        dest.writeString(image);
        dest.writeString(groupName);
        dest.writeString(roomNumber);
        dest.writeString(additionalInfo);
        dest.writeString(className);
        dest.writeString(longitude);
        dest.writeString(latitude);
    }
}
