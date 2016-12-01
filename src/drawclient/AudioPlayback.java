package drawclient;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class uses the AudioInputStream for play
 * AudioInputStream does not contain any seeking and doesn't know how long
 * the audio file is.
 * This class also assumes that playback will happen when called and 
 * playback does not stop until the file is finished playing.
 * Pausing and resuming is not supported when using AudioInputStream
 *
 * @author rcarroll
 */
public class AudioPlayback implements Runnable
{
    File audioFile;
    AudioInputStream audioInputStream;
    AudioFormat audioFormat;
    DataLine.Info dataLineInfo;
    SourceDataLine audioLine;
    final int BUFFER_SIZE = 4096;
    byte[] bytesBuffer = new byte[BUFFER_SIZE];
    int bytesRead = -1;
    
    
    public AudioPlayback(String fileName)
    {
        try 
        {
            audioFile = new File(fileName);
            //Create a new AudioInputStream
            audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            //We need the audio format for playback
            audioFormat = audioInputStream.getFormat();
            //Create a new DataLine.Info with the audioFormat
            dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            //Create a new SourceDataLine for actual playback
            audioLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        }
        catch(Throwable ex)
        {
            System.out.println("Exception caught in AudioPlayback " + ex);
        }
    }
            
    @Override
    public void run() 
    {
        startPlayback();
    }
    
    private void startPlayback()
    {
        try 
        {
            //Begin playback
            audioLine.open(audioFormat);
            audioLine.start();
            //This is an audio buffer in-case there are any disk hiccups
             while((bytesRead = audioInputStream.read(bytesBuffer)) != -1)
             {
                 audioLine.write(bytesBuffer , 0, bytesRead);
             }
             //Stop audio playback after the file is finished playing
             stopPlayback();
        } 
        catch (Throwable ex) 
        {
            System.out.println("Exception caught in startPlayback" + ex);
        }
    }
    
    public void stopPlayback()
    {
        try {
            audioLine.flush();
            audioLine.close();
            audioInputStream.close();
        } catch (Throwable ex) {
            System.out.println("Exception caught in stopPlayback" + ex);
        }
    }
    
}
