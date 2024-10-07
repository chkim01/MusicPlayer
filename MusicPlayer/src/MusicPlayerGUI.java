import javax.sound.sampled.*;  // 오디오 파일 처리에 필요한 패키지
import javax.swing.*;   // GUI를 만들기 위한 패키지
import java.awt.event.*;  // 이벤트 처리를 위한 패키지
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerGUI extends JFrame {  // JFrame은 윈도우 창을 나타냄
    private JButton playButton;   // 재생 버튼
    private JButton pauseButton;  // 일시정지 버튼
    private JButton stopButton;   // 정지 버튼
    private JButton nextButton;  // 다음 버튼
    private JButton previousButton;  // 이전 곡 버튼 추가
    private JButton selectFilesButton;  // 파일 선택 버튼 추가
    private JList<String> playListUI;   // 재생 목록을 보여줄 JList
    private DefaultListModel<String> listModel;  // JList 모델
    private JSlider volumeSlider;  // 볼륨 조절 슬라이더 추가
    private Clip audioClip;  // 음악 재생을 위한 Clip 객체
    private JFileChooser fileChooser;  // 파일 선택 대화상자
    private long clipTimePosition;  // 일시정지 시점의 위치를 저장하는 변수
    private List<String> playList;  // 재생 목록을 저장하는 리스트
    private int currentSongIndex;   // 현재 재생 중인 노래의 인덱스
    private FloatControl volumeControl;  // 볼륨 컨트롤을 위한 객체
    private JLabel songInfoLabel;  // 현재 재생 중인 곡 정보 라벨
    private JLabel songTimeLabel;  // 재생 시간 라벨
    private JLabel statusLabel;    // 재생 상태 라벨
    private Timer timer;           // 재생 시간 업데이트용 타이머


    public MusicPlayerGUI() {  // 생성자
        // 창 기본 설정
        setTitle("Music Player");  // 창 제목 설정
        setSize(600, 500);         // 창 크기 설정 (너비 600, 높이 450)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 창 닫기 버튼 설정
        setLayout(null);  // 레이아웃을 null로 설정하여 버튼 위치를 직접 조정
        
        // 파일 선택 버튼 추가
        selectFilesButton = new JButton("Select Files");
        selectFilesButton.setBounds(30, 300, 120, 30);
        add(selectFilesButton);

        // 재생 버튼 생성 및 설정
        playButton = new JButton("Play");  // 버튼의 텍스트 설정
        playButton.setBounds(160, 300, 80, 30);  // 위치(x, y) 및 크기(width, height) 설정
        add(playButton);  // 버튼을 창에 추가

        // 일지 정지 버튼 생성 및 설정
        pauseButton = new JButton("Pause");
        pauseButton.setBounds(250, 300, 80, 30);
        add(pauseButton);
        
        // 정지 버튼 생성 및 설정
        stopButton = new JButton("Stop");
        stopButton.setBounds(340, 300, 80, 30);  // 위치와 크기 설정
        add(stopButton);
        
        // 다음 버튼 생성 및 설정
        nextButton = new JButton("Next");
        nextButton.setBounds(430, 300, 80, 30);
        add(nextButton);
        
        // 이전 곡 버튼 추가
        previousButton = new JButton("Previous");
        previousButton.setBounds(520, 300, 80, 30);
        add(previousButton);
        
        // JList 모델 초기화
        listModel = new DefaultListModel<>();
        playListUI = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(playListUI);  // 스크롤을 추가하여 재생목록 길어질 때 대비
        scrollPane.setBounds(30, 30, 500, 250);
        add(scrollPane);
        
        // 볼륨 조절 슬라이더 추가
        volumeSlider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
        volumeSlider.setBounds(550, 30, 30, 250);  // 위치 및 크기 설정
        add(volumeSlider);
        
        // 곡 정보, 재생 시간, 상태를 표시할 라벨 추가
        songInfoLabel = new JLabel("Song: ");
        songInfoLabel.setBounds(30, 350, 300, 30);
        add(songInfoLabel);

        songTimeLabel = new JLabel("Time: 00:00 / 00:00");
        songTimeLabel.setBounds(30, 380, 300, 30);
        add(songTimeLabel);

        statusLabel = new JLabel("Status: Stopped");
        statusLabel.setBounds(30, 410, 300, 30);
        add(statusLabel);
        
        // 파일 선택 창 초기화
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));  // 기본 디렉토리를 현재 디렉토리로 설정
        fileChooser.setMultiSelectionEnabled(true);      // 여러 개의 파일 선택 가능하게 설정
        
        playList = new ArrayList<>();  // 재생 목록을 위한 리스트 초기화
        currentSongIndex = -1;          // 재생 목록에서 첫 번째 곡의 인덱스
        
        /*
        // Play 버튼 클릭 시 파일 선택 후 오디오 재생
        playButton.addActionListener(new ActionListener() {  // 재생 버튼을 클릭했을 때
            @Override
            public void actionPerformed(ActionEvent e) {
            	// 여기에서 음악 재생 로직을 추가할 예정
            	int result = fileChooser.showOpenDialog(null);  // 파일 선택 대화상자 표시
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();  // 선택한 여러 파일 가져오기
                    for (File file : selectedFiles) {
                        playList.add(file.getAbsolutePath());  // 파일 경로를 리스트에 추가
                        listModel.addElement(file.getName());  // 파일 이름을 JList에 추가
                    }
                    if (currentSongIndex == -1 && !playList.isEmpty()) {
                        currentSongIndex = 0;  // 첫 번째 곡으로 설정
                        playMusic(playList.get(currentSongIndex));
                    }
                }
            }
        });
        */
        
        // 1. 파일 선택 버튼 클릭 시 파일을 선택하고 재생 목록에 추가
        selectFilesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = fileChooser.showOpenDialog(null);  // 파일 선택 대화상자 표시
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();  // 선택한 여러 파일 가져오기
                    for (File file : selectedFiles) {
                        playList.add(file.getAbsolutePath());  // 파일 경로를 리스트에 추가
                        listModel.addElement(file.getName());  // 파일 이름을 JList에 추가
                    }
                }
            }
        });

        // 2. Play 버튼 클릭 시 선택된 곡 재생
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (playListUI.getSelectedIndex() != -1) {
                    currentSongIndex = playListUI.getSelectedIndex();  // 선택된 곡 인덱스
                    playMusic(playList.get(currentSongIndex));  // 선택한 곡 재생
                } else if (currentSongIndex != -1) {
                    playMusic(playList.get(currentSongIndex));  // 현재 곡 계속 재생
                }
            }
        });

        // Pause 버튼 클릭 시 오디오 일시정지
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (audioClip != null && audioClip.isRunning()) {
                    clipTimePosition = audioClip.getMicrosecondPosition();  // 현재 위치 저장
                    audioClip.stop();  // 오디오 일시정지
                    statusLabel.setText("Status: Paused");
                }
            }
        });
        
        // Stop 버튼 클릭 시 오디오 정지
        stopButton.addActionListener(new ActionListener() {  // 정지 버튼을 클릭했을 때
            @Override
            public void actionPerformed(ActionEvent e) {
                // 여기에서 음악 정지 로직을 추가할 예정
            	if (audioClip != null) {
                    audioClip.stop();  // 오디오 정지
                    audioClip.close(); // 오디오 클립 리소스 해제
                    clipTimePosition = 0;  // 재생 위치 초기화
                    statusLabel.setText("Status: Stopped");
                    stopTimer();  // 타이머 중지
                }
            }
        });
        
        // Next 버튼 클릭 시 다음 곡 재생
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!playList.isEmpty()) {
                    currentSongIndex = (currentSongIndex + 1) % playList.size();  // 다음 곡 인덱스로 이동 (순환 재생)
                    playMusic(playList.get(currentSongIndex));  // 다음 곡 재생
                    playListUI.setSelectedIndex(currentSongIndex);  // 재생 중인 곡 선택
                    statusLabel.setText("Status: Playing");
                }
            }
        });
        
        // 이전 곡 버튼 클릭 시 이전 곡 재생
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!playList.isEmpty()) {
                    // 이전 곡으로 이동, 첫 번째 곡이면 마지막 곡으로 돌아가기
                    currentSongIndex = (currentSongIndex - 1 + playList.size()) % playList.size();
                    playMusic(playList.get(currentSongIndex));
                    playListUI.setSelectedIndex(currentSongIndex);
                    statusLabel.setText("Status: Playing");
                }
            }
        });
        
        // 볼륨 슬라이더 변경 시 볼륨 조절
        volumeSlider.addChangeListener(e -> {
            if (audioClip != null && volumeControl != null) {
                float volume = volumeSlider.getValue() / 100f;  // 슬라이더 값을 비율로 변환
                setVolume(volume);  // 볼륨 설정
            }
        });
        
        // JList에서 곡을 클릭하면 선택한 곡을 재생
        playListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // 더블 클릭 시
                    currentSongIndex = playListUI.getSelectedIndex();  // 선택한 인덱스를 가져옴
                    if (currentSongIndex != -1) {
                        playMusic(playList.get(currentSongIndex));  // 해당 곡 재생
                    }
                }
            }
        });
    }
    
    // 오디오 재생 메서드
    private void playMusic(String filePath) {
        try {
        	if (audioClip != null && audioClip.isRunning()) {
                audioClip.stop();  // 재생 중인 클립 정지
                audioClip.close(); // 클립 리소스 해제
            }
        	
        	// 오디오 파일 로드
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            
            // 곡 정보 라벨 업데이트
            songInfoLabel.setText("Song: " + audioFile.getName());
            
            // 볼륨 컨트롤 초기화
            if (audioClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(volumeSlider.getValue() / 100f);  // 초기 볼륨 설정
            }
            
            // 일시정지 후 재생할 경우, 저장된 위치에서 재생
            if (clipTimePosition > 0) {
                audioClip.setMicrosecondPosition(clipTimePosition);  // 이전 위치에서 이어서 재생
            }
            
            // 오디오 재생
            audioClip.start();
            statusLabel.setText("Status: Playing");

            startTimer();  // 타이머 시작하여 재생 시간 업데이트
            
            // 재생이 끝났을 때 상태를 "Stopped"로 업데이트
            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && audioClip.getMicrosecondPosition() == audioClip.getMicrosecondLength()) {
                    statusLabel.setText("Status: Stopped");
                    stopTimer();  // 타이머 중지
                }
            });
            
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {  // 지원되지 않는 오디오 파일 형식, 오디오 라인 사용 불가, 파일 입출력 예외 처리
            e.printStackTrace();
        }
    }
    
    // 볼륨 설정 메서드
    private void setVolume(float volume) {
        if (volumeControl != null) {
            float minVolume = volumeControl.getMinimum();
            float maxVolume = volumeControl.getMaximum();
            float volumeRange = maxVolume - minVolume;
            float gain = minVolume + (volumeRange * volume);  // 비율에 따라 볼륨 설정
            volumeControl.setValue(gain);  // 볼륨 조절
        }
    }
    
    // 타이머 시작 메서드: 재생 시간 업데이트
    private void startTimer() {
    	stopTimer();  // 기존 타이머가 있을 경우 중지

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = audioClip.getMicrosecondPosition() / 1000000;  // 현재 재생 시간 (초)
                long totalTime = audioClip.getMicrosecondLength() / 1000000;  // 총 재생 시간 (초)
                String timeStr = String.format("Time: %02d:%02d / %02d:%02d",
                        currentTime / 60, currentTime % 60, totalTime / 60, totalTime % 60);
                songTimeLabel.setText(timeStr);
            }
        }, 0, 1000);  // 1초마다 갱신
    }

    // 타이머 중지 메서드
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    // main 메서드 (프로그램 실행 시작 지점)
    public static void main(String[] args) {
        MusicPlayerGUI player = new MusicPlayerGUI();  // MusicPlayerGUI 객체 생성
        player.setVisible(true);  // 창을 보이도록 설정
    }
}
