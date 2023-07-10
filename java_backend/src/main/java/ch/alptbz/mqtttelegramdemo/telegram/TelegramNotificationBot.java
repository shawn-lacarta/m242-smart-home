package ch.alptbz.mqtttelegramdemo.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TelegramNotificationBot
        extends Thread implements UpdatesListener, TelegramSenderInterface {

    private final TelegramBot bot;
    private final List<Long> users = Collections.synchronizedList(new ArrayList<Long>());

    private final ArrayList<TelegramConsumerInterface> consumers = new ArrayList<>();

    public TelegramNotificationBot(String botToken) {
        bot = new TelegramBot(botToken);

        bot.setUpdatesListener(this);


    }

    public void addHandler(TelegramConsumerInterface consumer) {
        consumers.add(consumer);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null || update.message().text() == null) {
                continue;
            }
            String message = update.message().text();
            if (message.startsWith("/help")) {
                SendMessage reply = new SendMessage(update.message().chat().id(),
                        """
                                Use /subscribe to get alarm activity updates.\040
                                Use /unsubscribe to stop getting alarm activity updates.\040
                                Use /apartment-room-x to get access to one specific room (replace x with 1 | 2 | 3)\040
                                Use /apartment-color-x to change the color of the lights in the room (replace x with red | white | blue)\040
                                Use /apartment-status-x to change the status of the lights in the room (replace x with on | off | blink)\040
                                Use /apartment-x-y to get access to a room and to sync the system with the pre-saved configuration (replace x with 1 | 2 | 3) (replace y with day | sleep| leaving)\040
                                """);
                bot.execute(reply);
            } else if (message.startsWith("/subscribe")) {
                if (!users.contains(update.message().chat().id())) {
                    users.add(update.message().chat().id());
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "Welcome! Use /unsubscribe to stop getting alarm activity updates.");
                    bot.execute(reply);
                } else {
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "You are already subscribed to the alarm activity updates!");
                    bot.execute(reply);
                }
            } else if (message.startsWith("/unsubscribe")) {
                if (users.contains(update.message().chat().id())) {
                    users.remove(update.message().chat().id());
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "You will no longer receive alarm activity updates.");
                    bot.execute(reply);
                } else {
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "You cannot unsubscribe something you've never subscribed to.");
                    bot.execute(reply);
                }
            }
            for (TelegramConsumerInterface consumer : consumers) {
                if (consumer.acceptsCommand(message)) {
                    consumer.handleCommand(update, message);
                }
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Override
    public void sendMessage(Long telegramUserId, String message) {
        SendMessage newMessage = new SendMessage(telegramUserId, message);
        bot.execute(newMessage);
    }

    @Override
    public void sendToAllSubscribers(String message) {
        for (Long user : users) {
            SendMessage reply = new SendMessage(user, message);
            bot.execute(reply);
        }
    }

    @Override
    public void sendReply(Update update, String message) {
        SendMessage reply = new SendMessage(update.message().chat().id(), message);
        bot.execute(reply);
    }
}
