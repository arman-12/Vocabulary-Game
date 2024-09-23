import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WordGuessingGame {
    private JLabel guiCategory;
    private JLabel guiFirstLetter;
    private String selectedDifficulty;
    private JFrame difficultyFrame;
    private JFrame gameFrame;
    private JPanel difficultyPanel;
    private JPanel gamePanel;
    private String playedWord;  
    private char[] gameboard;
    private char[] gameboardFinished;
    private java.util.List<String> guessArchive = new ArrayList<>();
    // Initializing lives as an ArrayList with 5 lives
    private java.util.List<String> lives = new ArrayList<>(Arrays.asList("Lives(5):","❤️","❤️","❤️","❤️","❤️"));
    private boolean endState = false;
    private int mouseX = 350, mouseY = 260; // Starting position of the mouse
    private int cheeseX = 600, cheeseY = 50; // Position of the cheese
    private JLabel mouseLabel;
    private JLabel cheeseLabel;
    private JLabel stairLabel;
    private JLabel guiGameboard;
    private JLabel guiGuessArchive;
    private JLabel guiLives;
    private Map<String, Boolean> buttonStates = new HashMap<>();

    private static final Map<String, Map<String, String[]>> wordLists = new HashMap<>();

    static {
        wordLists.put("Beginner", new HashMap<>() {{
            put("animals", new String[]{"cat", "dog", "bat", "fox", "cow", "pig", "rat", "ant", "bee", "hen"});
            put("fruits", new String[]{"apple", "pear", "plum", "lime", "kiwi", "peach", "date", "fig", "berry", "nut"});
            put("colors", new String[]{"red", "blue", "green", "pink", "gray", "gold", "brown", "cyan", "teal", "white"});
            put("objects", new String[]{"pen", "mug", "book", "desk", "chair", "shoe", "ball", "door", "lamp", "key"});
            put("verbs", new String[]{"run", "jump", "swim", "play", "read", "write", "sing", "walk", "talk", "cook"});
        }});

        wordLists.put("Intermediate", new HashMap<>() {{
            put("animals", new String[]{"zebra", "whale", "panda", "shark", "tiger", "horse", "lemur", "koala", "otter", "raven", "skunk", "frogs"});
            put("fruits", new String[]{"peach", "melon", "mango", "grape", "lemon", "apple", "berry", "grape", "melon"});
            put("flowers", new String[]{"tulip", "rose", "daisy", "poppy", "violet", "tulip", "daisy", "poppy", "violet"});
        }});

        wordLists.put("Advanced", new HashMap<>() {{
            put("animals", new String[]{"hippo", "rhino", "crocs", "chimp", "lemur", "hyena", "dingo", "whale", "shark"});
            put("fruits", new String[]{"pomeg", "persi", "grape", "mango", "quinc", "guava", "dates", "plant", "lemon", "lyche", "apric", "papay", "mulbe", "berry", "prune", "mango", "grape"});
            put("flowers", new String[]{"chrys", "hydrn", "anemo", "camel", "clemi", "tulip", "daisy", "poppy", "daffo", "vibur", "mimic", "dahli"});
        }});
    }

    public WordGuessingGame() {
        createDifficultyFrame();
    }

    public void createDifficultyFrame() {
        difficultyFrame = new JFrame("Word Guessing Game");
        difficultyFrame.setSize(950, 630);
        difficultyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        difficultyPanel = new JPanel();
        difficultyPanel.setLayout(null);

        JButton beginnerButton = new JButton(new ImageIcon("beginner_image.png"));
        beginnerButton.setBounds(530, 250, 200, 50);
        beginnerButton.addActionListener(e -> {
            playSound("click_sound.wav"); // Play click sound
            setSelectedDifficulty("Beginner");
        });
        difficultyPanel.add(beginnerButton);
        

        JButton intermediateButton = new JButton(new ImageIcon("intermediate_image.png"));
        intermediateButton.setBounds(530, 320, 200, 50);
        intermediateButton.addActionListener(e -> {
            playSound("click_sound.wav"); // Play click sound
            setSelectedDifficulty("Intermediate");
        });
        difficultyPanel.add(intermediateButton);

        JButton advancedButton = new JButton(new ImageIcon("advanced_image.png"));
        advancedButton.setBounds(530, 390, 200, 50);
        advancedButton.addActionListener(e -> {
            playSound("click_sound.wav"); // Play click sound
            setSelectedDifficulty("Advanced");
        });
        difficultyPanel.add(advancedButton);

        JButton playButton = new JButton(new ImageIcon("play_image.png"));
        playButton.setBounds(525, 460, 200, 50);
        playButton.addActionListener(e -> {
            playSound("click_sound.wav"); // Play click sound
            startGame();
        });
        difficultyPanel.add(playButton);

        difficultyFrame.add(difficultyPanel);
        difficultyFrame.setVisible(true);
    }

    public void setSelectedDifficulty(String difficulty) {
        selectedDifficulty = difficulty;
    }

    public void startGame() {
        if (selectedDifficulty != null) {
            difficultyFrame.dispose();
            gameFrame = new JFrame("Word Guessing Game");
            gameFrame.setSize(950, 630);
            gameFrame.setResizable(true);
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
            gamePanel = new JPanel();
            gamePanel.setLayout(null);
    
            playedWord = getRandomWord(selectedDifficulty);
            gameboard = new char[playedWord.length()];
            Arrays.fill(gameboard, '_');
            gameboardFinished = playedWord.toCharArray();
    
            guiGameboard = new JLabel(new String(gameboard));
            guiGameboard.setFont(new Font("Verdana", Font.BOLD, 30));
            guiGameboard.setBounds(100, 50, 800, 50);
            gamePanel.add(guiGameboard);
    
            // Display category hint
            String category = getWordCategory(playedWord);
            guiCategory = new JLabel("Category: " + category);
            guiCategory.setFont(new Font("Verdana", Font.BOLD, 15));
            guiCategory.setBounds(100, 100, 800, 30);
            gamePanel.add(guiCategory);
    
            // Display first letter hint
            guiFirstLetter = new JLabel("First Letter: " + playedWord.charAt(0));
            guiFirstLetter.setFont(new Font("Verdana", Font.BOLD, 15));
            guiFirstLetter.setBounds(100, 130, 800, 30);
            gamePanel.add(guiFirstLetter);
    
            guiGuessArchive = new JLabel("Guesses:");
            guiGuessArchive.setFont(new Font("Verdana", Font.BOLD, 10));
            guiGuessArchive.setBounds(100, 300, 800, 30);
            gamePanel.add(guiGuessArchive);
    
            guiLives = new JLabel(lives.toString());
            guiLives.setFont(new Font("Verdana", Font.BOLD, 10));
            guiLives.setBounds(100, 330, 800, 30);
            gamePanel.add(guiLives);
    
            setupGameGraphics();
            populateBoard();
    
            gameFrame.add(gamePanel);
            gameFrame.setVisible(true);
        }
    }
    
    public String getRandomWord(String difficulty) {
        Map<String, String[]> categories = wordLists.get(difficulty);
        String category = new ArrayList<>(categories.keySet()).get(new Random().nextInt(categories.keySet().size()));
        return categories.get(category)[new Random().nextInt(categories.get(category).length)];
    }
    private String getWordCategory(String word) {
        Map<String, String[]> categories = wordLists.get(selectedDifficulty);
        for (Map.Entry<String, String[]> entry : categories.entrySet()) {
            String category = entry.getKey();
            String[] words = entry.getValue();
            for (String w : words) {
                if (w.equals(word)) {
                    return category; // Return the category of the word found
                }
            }
        }
        return "unknown"; // Return a default value if not found
    }
    private void setupGameGraphics() {
        int wordLength = playedWord.length();
        switch(wordLength)
        {
            case 1:
            cheeseX = 395 ;
            cheeseY = 267 ;
            break;
            case 2:
            cheeseX = 415 ;
            cheeseY = 245 ;
            break;
            case 3:
            cheeseX = 438 ;
            cheeseY = 222 ;
            break;
            case 4:
            cheeseX = 461 ;
            cheeseY = 198 ;
            break;
            case 5:
            cheeseX = 483 ;
            cheeseY = 176 ;
            break;
            case 6:
            cheeseX = 505 ;
            cheeseY = 154 ;
            break;
            case 7:
            cheeseX = 538 ;
            cheeseY = 130 ;
            break;
            case 8:
            cheeseX = 550 ;
            cheeseY = 108 ;
            break;
            case 9:
            cheeseX = 598 ;
            cheeseY = 63 ;
        }

       

        // Load and set up the stair image
        stairLabel = new JLabel(new ImageIcon("stair.png"));
        stairLabel.setBounds(300, 10, 400, 400); // Position and size of the stairs
        gamePanel.add(stairLabel);

        // Load and set up the mouse image
        mouseLabel = new JLabel(new ImageIcon("mouse.png"));
        mouseLabel.setBounds(mouseX, mouseY, 50, 50); // Set initial mouse position and size (50x50)

        // Resizing the mouse image to 50x50 pixels
        ImageIcon originalMouseIcon = new ImageIcon("mouse.png");
        Image resizedMouseImage = originalMouseIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        ImageIcon resizedMouseIcon = new ImageIcon(resizedMouseImage);

        // Apply the resized image to the JLabel
        mouseLabel.setIcon(resizedMouseIcon);  

        // Add the mouse label to the game panel
        gamePanel.add(mouseLabel);

        // Load and set up the cheese image
        cheeseLabel = new JLabel(new ImageIcon("cheese.png"));
        cheeseLabel.setBounds(cheeseX, cheeseY, 25, 25); // Set initial cheese position and size (30x30)

        // Resizing the cheese image to 30x30 pixels
        ImageIcon originalCheeseIcon = new ImageIcon("cheese.png");
        Image resizedCheeseImage = originalCheeseIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon resizedCheeseIcon = new ImageIcon(resizedCheeseImage);

        // Apply the resized image to the JLabel
        cheeseLabel.setIcon(resizedCheeseIcon);

        // Add the cheese label to the game panel
        gamePanel.add(cheeseLabel);
    }

    private void populateBoard() {
        int xpos = 150;
        int ypos = 370;
        int c = 0;

        while (c < 26) {
            if (c == 6) {
                ypos = 420;
                xpos = 150;
            } else if (c == 12) {
                ypos = 470;
                xpos = 150;
            } else if (c == 18) {
                ypos = 520;
                xpos = 150;
            } else if (c == 24) {
                ypos = 570;
                xpos = 150;
            }

            char letter = (char) ('a' + c);
            JButton button = new JButton(String.valueOf(letter).toUpperCase());
            button.setBounds(xpos, ypos, 100, 50);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!buttonStates.getOrDefault(String.valueOf(letter), false)) {
                        buttonStates.put(String.valueOf(letter), true);
                        getUserGuess(letter);
                        guiGameboard.setText(new String(gameboard));
                        guiGuessArchive.setText(guessArchive.toString());
                        guiLives.setText(lives.toString());
                        checkEndState();
                    }
                }
            });
            gamePanel.add(button);
            xpos += 100;
            c++;
        }
    }
    private void showMessage(String message) {
        // Get the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Create a JOptionPane
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        
        // Create a dialog from the JOptionPane
        JDialog dialog = pane.createDialog(gameFrame, "Message");
        
        // Set the dialog location near the bottom of the screen
        int dialogWidth = dialog.getWidth();
        int dialogHeight = dialog.getHeight();
        dialog.setLocation((screenSize.width - dialogWidth) / 2, screenSize.height - dialogHeight - 200); // 100 pixels from the bottom
    
        dialog.setVisible(true);
    }
    private boolean showConfirmDialog(String message) {
        playSound("click_sound.wav");
        // Get the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
        // Create a JOptionPane
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        
        // Create a dialog from the JOptionPane
        JDialog dialog = pane.createDialog(gameFrame, "Game Over");
    
        // Set the dialog location near the bottom of the screen
        int dialogWidth = dialog.getWidth();
        int dialogHeight = dialog.getHeight();
        dialog.setLocation((screenSize.width - dialogWidth) / 2, screenSize.height - dialogHeight - 200); // 100 pixels from the bottom
    
        dialog.setVisible(true);
        
    
        // Get the user's response
        Object selectedValue = pane.getValue();
        if (selectedValue != null && selectedValue.equals(JOptionPane.YES_OPTION)) {
            playSound("click_sound.wav");
            return true;
        } else {
            playSound("click_sound.wav");
            return false;
        }
        
    }

    private void getUserGuess(char letter) {
        if (guessArchive.contains(String.valueOf(letter))) {
            JOptionPane.showMessageDialog(gameFrame, "Letter already guessed!");
        } else {
            boolean correctGuess = false;
            guessArchive.add(String.valueOf(letter));
            for (int i = 0; i < gameboardFinished.length; i++) {
                if (gameboardFinished[i] == letter) {
                    gameboard[i] = letter;
                    correctGuess = true;
                }
            }

            if (correctGuess) {
                animateCorrectGuess();
            } else {
                playSound("wrong_guess.wav");
                if (lives.size() == 0) {
                    playSound("game_over.wav");
                    showMessage("You lost! The word was: " + playedWord);
                    endState = true;
                } else {
                    lives.remove(lives.size() - 1);
                }
            }
        }
    }

    private void animateCorrectGuess() {
        // Move the mouse closer to the cheese along the stairs
        if (mouseY > cheeseY) {
            mouseY -= 22.1;  // Move mouse upwards
            mouseX += 22;     // Slightly move right to simulate diagonal movement
        }
    
        // Update the mouse's position
        mouseLabel.setBounds(mouseX, mouseY, 50, 50);
    
        playSound("correct_guess.wav");
    
        // Check if the guessed word is fully completed
        if (Arrays.equals(gameboard, gameboardFinished)) {
            // Make the mouse and cheese disappear
            gamePanel.remove(mouseLabel);
            gamePanel.remove(cheeseLabel);
    
            // Load and resize the new image "mouse_cheese.png"
            ImageIcon originalNewImageIcon = new ImageIcon("mouse_cheese.png");
            Image resizedNewImage = originalNewImageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            ImageIcon resizedNewImageIcon = new ImageIcon(resizedNewImage);
    
            // Display the resized new image at the cheese position
            JLabel newImageLabel = new JLabel(resizedNewImageIcon);
            newImageLabel.setBounds(cheeseX-150, cheeseY-160, 300, 300); // Set size and position of new image
            gamePanel.add(newImageLabel);
    
            // Repaint the game panel to update the changes
            gamePanel.repaint();
            // Play the win sound
            playSound("win_sound.wav");
            // Show winning message
            showMessage("Congratulations! You guessed the word: " + playedWord);
            // Set the end state to true to stop the game
            endState = true;
        }
    }
    private void playSound(String soundFile) {
        try {
            File soundPath = new File(soundFile);
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void checkEndState() {
        if (endState) {
            boolean response = showConfirmDialog("Would you like to play again?");
            if (response) {
                gameFrame.dispose();
                new WordGuessingGame();
            } else {
                gameFrame.dispose();
            }
        }
    }

    public static void main(String[] args) {
        new WordGuessingGame();
    }
}