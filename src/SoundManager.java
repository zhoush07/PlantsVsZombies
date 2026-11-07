import javax.sound.sampled.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

/** 使用 Java 自带音频接口生成轻量音乐和音效；不需要 MP3 播放库。 */
public final class SoundManager {
    public enum Sound { CLICK, PLANT, SHOOT, BITE, BOOM, SUN, WIN, LOSE }
    private static final AudioFormat FORMAT=new AudioFormat(22050,16,1,true,false);
    private static final Map<Sound,Clip> clips=new EnumMap<>(Sound.class);
    private static final Map<Sound,Long> lastPlayed=new ConcurrentHashMap<>();
    private static final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,0,
            TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(32),task->{
        Thread thread=new Thread(task,"game-audio");thread.setDaemon(true);return thread;
    },new ThreadPoolExecutor.DiscardPolicy());
    private static volatile boolean muted, paused, musicEnabled;
    private static boolean initialized, available=true;
    private static Clip music;
    private SoundManager() {}

    public static boolean isMuted(){return muted;}
    public static void setMuted(boolean value){muted=value;sync();}
    public static void setPaused(boolean value){paused=value;sync();}
    public static void setMusicEnabled(boolean value){musicEnabled=value;sync();}
    private static void sync(){worker.execute(()->{initialize();synchronizeMusic();});}

    public static void play(Sound sound) {
        if(muted||paused||Boolean.getBoolean("game.audio.disabled"))return;
        long now=System.nanoTime();
        Long last=lastPlayed.put(sound,now);
        // 多颗子弹同一时间发射时合并音效，避免声音叠加过响。
        if(last!=null && now-last<90_000_000L)return;
        worker.execute(()->{
            initialize();
            if(!available||muted||paused||System.nanoTime()-now>500_000_000L)return;
            Clip clip=clips.get(sound);
            if(clip!=null){clip.stop();clip.setFramePosition(0);clip.start();}
        });
    }
    private static void initialize(){
        if(initialized)return;
        initialized=true;
        if(Boolean.getBoolean("game.audio.disabled")){available=false;return;}
        try{
            music=open(makeMusic(),0.35f);
            for(Sound sound:Sound.values())clips.put(sound,open(makeEffect(sound),0.65f));
        }catch(Exception e){
            available=false;
            if(music!=null)music.close();
            for(Clip clip:clips.values())clip.close();
            clips.clear();
            System.err.println("音频设备不可用，游戏继续静音运行："+e.getMessage());
        }
    }
    private static Clip open(byte[] data,float volume)throws Exception{
        Clip clip=AudioSystem.getClip();
        try{
            clip.open(FORMAT,data,0,data.length);
            if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)){
                FloatControl gain=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                float db=(float)(20*Math.log10(volume));
                gain.setValue(Math.max(gain.getMinimum(),Math.min(gain.getMaximum(),db)));
            }
            return clip;
        }catch(Exception e){clip.close();throw e;}
    }
    private static void synchronizeMusic(){
        if(!available)return;
        if(muted||paused){
            for(Clip clip:clips.values())clip.stop();
        }
        if(musicEnabled&&!muted&&!paused){
            if(!music.isRunning())music.loop(Clip.LOOP_CONTINUOUSLY);
        }else music.stop();
    }
    private static void sample(byte[] data,int index,double value){
        short n=(short)(Math.max(-1,Math.min(1,value))*32767);
        data[index*2]=(byte)n;data[index*2+1]=(byte)(n>>8);
    }
    // 原创简短循环旋律，约十秒；末尾自然淡出，循环不会产生爆音。
    private static byte[] makeMusic(){
        int[] melody={60,64,67,64,62,65,69,65,59,62,67,62,60,64,67,72,
                69,65,64,60,67,64,62,59,60,64,67,64,62,59,60,60};
        int rate=22050, length=(int)(rate*0.32*melody.length);
        byte[] data=new byte[length*2];
        for(int i=0;i<length;i++){
            double t=i/(double)rate;
            int note=Math.min(melody.length-1,(int)(t/0.32));
            double local=t-note*0.32;
            double envelope=Math.min(1,local/0.015)*Math.max(0,1-local/0.32);
            double hz=440*Math.pow(2,(melody[note]-69)/12.0);
            sample(data,i,envelope*(0.16*Math.sin(2*Math.PI*hz*local)
                    +0.07*Math.sin(2*Math.PI*hz/2*local)));
        }
        return data;
    }
    private static byte[] makeEffect(Sound sound){
        double duration=switch(sound){case BOOM->0.5;case WIN,LOSE->0.9;case BITE->0.16;default->0.12;};
        int rate=22050,length=(int)(rate*duration);
        byte[] data=new byte[length*2];Random noise=new Random(sound.ordinal());
        for(int i=0;i<length;i++){
            double t=i/(double)rate, fraction=t/duration;
            double envelope=Math.min(1,t/0.006)*Math.pow(1-fraction,2);
            double frequency=switch(sound){
                case CLICK->650;case PLANT->180;case SHOOT->700-400*fraction;
                case BITE->100;case BOOM->55;case SUN->900+500*fraction;
                case WIN->new double[]{523,659,784}[Math.min(2,(int)(fraction*3))];
                case LOSE->new double[]{392,330,262}[Math.min(2,(int)(fraction*3))];
            };
            double value=Math.sin(2*Math.PI*frequency*t)*0.25;
            if(sound==Sound.BOOM||sound==Sound.BITE)value+=(noise.nextDouble()*2-1)*0.3;
            sample(data,i,envelope*value);
        }
        return data;
    }
}
