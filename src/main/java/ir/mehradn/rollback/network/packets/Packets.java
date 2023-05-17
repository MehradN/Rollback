package ir.mehradn.rollback.network.packets;

public interface Packets {
    OpenGUI openGui = new OpenGUI();
    FetchMetadata fetchMetadata = new FetchMetadata();
    SendMetadata sendMetadata = new SendMetadata();
}
