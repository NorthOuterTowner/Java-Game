package client.run;
import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
public class AudioPlayer {
    /*public static void main(String[] args) {
        playSound("tst.wav");
    }*/
    public static void playSound(String fileName) {
        URL url = AudioPlayer.class.getResource(fileName);
        System.out.println(url);
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            AudioFormat format = audioIn.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            Thread.sleep(clip.getMicrosecondLength() / 1000);
            audioIn.close();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
