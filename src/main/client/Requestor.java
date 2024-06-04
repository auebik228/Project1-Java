package main.client;

import commands.*;
import utils.CollectionHandler;
import utils.ConsoleAdministrator;
import utils.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Requestor {
    private NetWork netWork;
    private byte[] requestBytes;
    private byte[] answerBytes = new byte[100000];

    public Requestor(NetWork netWork) {
        this.netWork = netWork;
    }

    private boolean sendRequest(String user) throws IOException {
        AbstractCommand command = ConsoleAdministrator.commandRequest();
        if (command.getName() == CommandNames.exit) {
            new Exit().use();
        }
        if (command.getName() == CommandNames.save) {
            System.out.println("Данная команда недоступна на клиенте");
            command = new VoidCommand();
        }
        if (command.getName() == CommandNames.exitAccount){
            CommandManager.useCommand(command);
            System.out.println("Вы вышли из аккаунта, без аккаунта нельзя работать");
            return false;
        }
        if(command.getName() == CommandNames.executeScript){
            ExecuteScript executeScript = (ExecuteScript) command;
            executeScript.readCommandsFromFile();
        }
        if (command.getName() != CommandNames.voidCommand) {
            if (command instanceof AddingCommand) {
                AddingCommand command1 = (AddingCommand) command;
                command1.ticketRequest();
                command1.getTicket().setOwner(user);
                command1.setUser(user);
                requestBytes = Serializer.serializeObject(command1);
            } else {
                command.setUser(user);
                requestBytes = Serializer.serializeObject(command);
            }
            netWork.getSocketOut().write(requestBytes);
            return true;
        }
        return false;
    }

    private String getAnswer() throws IOException {
        netWork.getSocketInput().read(answerBytes);
        ByteBuffer buffer = ByteBuffer.wrap(answerBytes);
        String string = (String) Serializer.deserializeObject(buffer);
        return string;
    }

    public void query(String user) throws IOException {
        boolean t;
        String s;
            t = sendRequest(user);
            if (t == true) {
                s = getAnswer();
                System.out.println(s);
        }
    }

}