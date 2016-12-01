package drawclient;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author rcarroll
 */
public class AudioRecorder implements Runnable 
{
    //Reference for the DrawClient
    private DrawClient drawClientReference;
    //Constants for Recording settings
    final float RECORDING_SAMPLE_RATE = 16000;
    final int RECORDING_SAMPLE_SIZE = 16;
    final int RECORDING_CHANNELS = 2;
    final boolean RECORDING_SIGNED = true;
    final boolean RECORDING_BIG_ENDIAN = true;
    
    //TargetDataLine that the audio is captured from
    TargetDataLine targetDataLine;
    
    //This needs to be changed to use a user defined filename and format
    String fileFormat = "wav";
    String fileName = "Audio" + "." + fileFormat;
    File file = new File(fileName);
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    
    @Override
    public void run() 
    {
        startRecording();
    }
    
    private void startRecording() 
    {
        try
        {
            AudioFormat audioFormat = new AudioFormat(RECORDING_SAMPLE_RATE, 
                    RECORDING_SAMPLE_SIZE, RECORDING_CHANNELS, 
                    RECORDING_SIGNED, RECORDING_BIG_ENDIAN);
            DataLine.Info dataLineInfo = 
                    new DataLine.Info(TargetDataLine.class, audioFormat);
            //We need to check if the system is capable of recording
            //if not we need to leave the thread
            if(!AudioSystem.isLineSupported(dataLineInfo))
            {
                System.out.println("Line is not supported");
                return;
            }
            //We need to get the system's DataLine.Info and cast it to
            //our targetDataLine so that we can record
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            //open a new TargetDataLine using the audioFormat
            targetDataLine.open(audioFormat);
            //Start capturing
            targetDataLine.start();
            AudioInputStream audioInputStream = 
                    new AudioInputStream(targetDataLine);
            //Start recording
            AudioSystem.write(audioInputStream, fileType, file);
            System.out.println("Past the .write");
        } 
        catch(Throwable ex) 
        {
            System.out.println("Something went wrong with the audio recorder: " 
                    + ex.getMessage());
        }
    }
    
    public void stopRecording() 
    {
        targetDataLine.flush();
        targetDataLine.stop();
        targetDataLine.close();
    }
}
