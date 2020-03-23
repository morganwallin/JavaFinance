package src;

import java.util.ArrayList;

public class MonitorNotifications {
    ArrayList<StockNotification> arrayList = new ArrayList<>();

    MonitorNotifications() {
    }

    MonitorNotifications(ArrayList<StockNotification> arrayList) {
        this.arrayList = arrayList;
    }

    public static void main(String[] args) {
        SendEmail sendEmail = new SendEmail("morgan_wallin@hotmail.com", "subject", "text");
        Thread thread = new Thread(sendEmail);
        thread.start();
    }
}
