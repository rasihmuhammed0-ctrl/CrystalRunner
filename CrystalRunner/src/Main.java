import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CoinQuest - a six-level Java Swing 2D platform game.
 * Compile: javac CrystalRunner.java
 * Run:     java CrystalRunner
 */
class CrystalRunner extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 560;
    private static final int FLOOR = 470;
    private static final int PLAYER_W = 34;
    private static final int PLAYER_H = 46;

    private final Timer timer = new Timer(16, this);
    private final Random random = new Random();

    private int level = 1;
    private int playerX = 55;
    private int playerY = FLOOR - PLAYER_H;
    private double velocityY = 0;
    private boolean onGround = true;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean facingRight = true;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean itemCollected = false;
    private boolean exitUnlocked = false;
    private int animationTick = 0;
    private String message = "";
    private int messageTicks = 0;

    private LevelData currentLevel;
    private final List<Rectangle> platforms = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private Rectangle collectible;
    private Rectangle exitDoor;

    private static class Enemy {
        double x;
        int y;
        int width;
        int height;
        double speed;
        int minX;
        int maxX;
        boolean spider;
        boolean movingRight = true;

        Enemy(int x, int y, int width, int height, double speed,
              int minX, int maxX, boolean spider) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
            this.minX = minX;
            this.maxX = maxX;
            this.spider = spider;
        }

        Rectangle bounds() {
            return new Rectangle((int) x, y, width, height);
        }

        void update() {
            x += movingRight ? speed : -speed;
            if (x <= minX) {
                x = minX;
                movingRight = true;
            } else if (x + width >= maxX) {
                x = maxX - width;
                movingRight = false;
            }
        }
    }

    private static class LevelData {
        String name;
        String weather;
        Color skyTop;
        Color skyBottom;
        Color ground;
        Color platform;
        Color accent;
        String itemName;
        int itemX;
        int itemY;
        int exitX;
        int exitY;
        int difficulty;
        int[][] platformData;
        int[][] enemyData; // x, y, width, height, speed*10, minX, maxX, type (1 spider, 0 scorpion)

        LevelData(String name, String weather, Color skyTop, Color skyBottom,
                  Color ground, Color platform, Color accent, String itemName,
                  int itemX, int itemY, int exitX, int exitY, int difficulty,
                  int[][] platformData, int[][] enemyData) {
            this.name = name;
            this.weather = weather;
            this.skyTop = skyTop;
            this.skyBottom = skyBottom;
            this.ground = ground;
            this.platform = platform;
            this.accent = accent;
            this.itemName = itemName;
            this.itemX = itemX;
            this.itemY = itemY;
            this.exitX = exitX;
            this.exitY = exitY;
            this.difficulty = difficulty;
            this.platformData = platformData;
            this.enemyData = enemyData;
        }
    }

    public CrystalRunner() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        setDoubleBuffered(true);
        addKeyListener(this);
        loadLevel(1);
        timer.start();
    }

    private LevelData getLevelData(int number) {
        switch (number) {
            case 1:
                return new LevelData(
                        "Green Meadow", "Sunny",
                        new Color(95, 190, 255), new Color(220, 245, 255),
                        new Color(65, 165, 65), new Color(120, 80, 45),
                        new Color(255, 220, 70), "Golden coin",
                        320, 355, 820, FLOOR - 72, 1,
                        new int[][] {
                                {150, 390, 105, 16}, {305, 350, 100, 16},
                                {465, 390, 100, 16}, {620, 345, 110, 16}
                        },
                        new int[][] {
                                {270, FLOOR - 28, 34, 28, 18, 220, 390, 0}
                        }
                );
            case 2:
                return new LevelData(
                        "Rainy Ruins", "Rain",
                        new Color(65, 85, 115), new Color(175, 195, 210),
                        new Color(75, 105, 80), new Color(100, 100, 110),
                        new Color(100, 240, 230), "Blue crystal",
                        390, 300, 820, FLOOR - 72, 2,
                        new int[][] {
                                {125, 400, 105, 16}, {275, 350, 100, 16},
                                {420, 305, 105, 16}, {570, 360, 100, 16},
                                {700, 315, 90, 16}
                        },
                        new int[][] {
                                {220, FLOOR - 28, 36, 28, 25, 160, 330, 0},
                                {600, FLOOR - 28, 36, 28, 20, 520, 710, 1}
                        }
                );
            case 3:
                return new LevelData(
                        "Desert Dunes", "Sandstorm",
                        new Color(245, 170, 75), new Color(255, 230, 160),
                        new Color(205, 150, 65), new Color(165, 105, 50),
                        new Color(255, 100, 30), "Ruby scarab",
                        535, 290, 820, FLOOR - 72, 3,
                        new int[][] {
                                {115, 390, 100, 16}, {250, 335, 95, 16},
                                {390, 285, 100, 16}, {535, 325, 95, 16},
                                {680, 365, 95, 16}
                        },
                        new int[][] {
                                {185, FLOOR - 28, 36, 28, 28, 125, 300, 0},
                                {420, FLOOR - 28, 38, 28, 30, 350, 535, 1},
                                {665, FLOOR - 28, 36, 28, 27, 590, 760, 0}
                        }
                );
            case 4:
                return new LevelData(
                        "Moonlit Forest", "Night mist",
                        new Color(20, 25, 65), new Color(70, 80, 120),
                        new Color(35, 95, 65), new Color(65, 75, 90),
                        new Color(170, 130, 255), "Moon gem",
                        475, 265, 820, FLOOR - 72, 4,
                        new int[][] {
                                {120, 395, 100, 16}, {255, 345, 100, 16},
                                {390, 295, 100, 16}, {530, 340, 100, 16},
                                {665, 390, 95, 16}
                        },
                        new int[][] {
                                // The spider's patrol ends well before the exit door.
                                {390, FLOOR - 30, 38, 30, 32, 330, 500, 1},
                                {180, FLOOR - 28, 36, 28, 30, 120, 285, 0},
                                {570, FLOOR - 28, 36, 28, 26, 535, 650, 0}
                        }
                );
            case 5:
                return new LevelData(
                        "Frozen Summit", "Snow",
                        new Color(105, 165, 220), new Color(230, 245, 255),
                        new Color(220, 235, 245), new Color(115, 155, 185),
                        new Color(80, 240, 255), "Ice star",
                        570, 255, 820, FLOOR - 72, 5,
                        new int[][] {
                                {105, 405, 95, 16}, {235, 360, 95, 16},
                                {365, 310, 95, 16}, {500, 265, 100, 16},
                                {640, 320, 95, 16}, {735, 375, 65, 16}
                        },
                        new int[][] {
                                {165, FLOOR - 28, 36, 28, 32, 100, 260, 1},
                                {360, FLOOR - 28, 36, 28, 35, 300, 475, 0},
                                {590, FLOOR - 28, 38, 28, 34, 520, 700, 1}
                        }
                );
            default:
                return new LevelData(
                        "Volcano Escape", "Ash and sparks",
                        new Color(85, 35, 40), new Color(230, 95, 45),
                        new Color(75, 55, 55), new Color(115, 65, 60),
                        new Color(255, 230, 60), "Flame heart",
                        505, 235, 820, FLOOR - 72, 6,
                        new int[][] {
                                {100, 405, 90, 16}, {220, 355, 90, 16},
                                {340, 305, 90, 16}, {460, 255, 90, 16},
                                {580, 305, 90, 16}, {700, 355, 90, 16}
                        },
                        new int[][] {
                                {140, FLOOR - 28, 36, 28, 36, 90, 235, 0},
                                {300, FLOOR - 28, 38, 28, 38, 260, 430, 1},
                                {500, FLOOR - 28, 36, 28, 40, 455, 630, 0},
                                {680, FLOOR - 28, 38, 28, 42, 620, 785, 1}
                        }
                );
        }
    }

    private void loadLevel(int number) {
        level = number;
        currentLevel = getLevelData(number);
        platforms.clear();
        enemies.clear();
        for (int[] p : currentLevel.platformData) {
            platforms.add(new Rectangle(p[0], p[1], p[2], p[3]));
        }
        for (int[] e : currentLevel.enemyData) {
            enemies.add(new Enemy(e[0], e[1], e[2], e[3], e[4] / 10.0,
                    e[5], e[6], e[7] == 1));
        }
        playerX = 55;
        playerY = FLOOR - PLAYER_H;
        velocityY = 0;
        onGround = true;
        movingLeft = false;
        movingRight = false;
        facingRight = true;
        itemCollected = false;
        exitUnlocked = false;
        gameOver = false;
        gameWon = false;
        collectible = new Rectangle(currentLevel.itemX, currentLevel.itemY, 26, 26);
        exitDoor = new Rectangle(currentLevel.exitX, currentLevel.exitY, 42, 72);
        setMessage("Collect the " + currentLevel.itemName + " to unlock the exit!", 180);
        requestFocusInWindow();
    }

    private void setMessage(String text, int ticks) {
        message = text;
        messageTicks = ticks;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !gameWon) {
            updateGame();
        }
        if (messageTicks > 0) messageTicks--;
        animationTick++;
        repaint();
    }

    private void updateGame() {
        double moveSpeed = 4.0 + (level - 1) * 0.15;
        if (movingLeft) {
            playerX -= (int) Math.ceil(moveSpeed);
            facingRight = false;
        }
        if (movingRight) {
            playerX += (int) Math.ceil(moveSpeed);
            facingRight = true;
        }
        playerX = Math.max(0, Math.min(WIDTH - PLAYER_W, playerX));

        int oldBottom = playerY + PLAYER_H;
        velocityY += 0.62;
        if (velocityY > 11) velocityY = 11;
        playerY += (int) Math.round(velocityY);
        onGround = false;

        if (playerY + PLAYER_H >= FLOOR) {
            playerY = FLOOR - PLAYER_H;
            velocityY = 0;
            onGround = true;
        }

        // Land on platforms only when falling from above.
        if (velocityY >= 0) {
            for (Rectangle p : platforms) {
                Rectangle playerNow = new Rectangle(playerX, playerY, PLAYER_W, PLAYER_H);
                if (playerNow.intersects(p) && oldBottom <= p.y + 8) {
                    playerY = p.y - PLAYER_H;
                    velocityY = 0;
                    onGround = true;
                }
            }
        }

        Rectangle playerBounds = new Rectangle(playerX + 4, playerY + 3,
                PLAYER_W - 8, PLAYER_H - 4);

        for (Enemy enemy : enemies) {
            enemy.update();
            if (playerBounds.intersects(enemy.bounds())) {
                gameOver = true;
                setMessage("You were caught! Press R to retry this level.", 999999);
                return;
            }
        }

        if (!itemCollected && playerBounds.intersects(collectible)) {
            itemCollected = true;
            exitUnlocked = true;
            setMessage(currentLevel.itemName + " collected! Reach the exit!", 180);
        }

        if (playerBounds.intersects(exitDoor)) {
            if (exitUnlocked) {
                if (level < 6) {
                    loadLevel(level + 1);
                } else {
                    gameWon = true;
                    setMessage("You completed all 6 levels! Press R to play again.", 999999);
                }
            } else if (messageTicks < 1) {
                setMessage("The exit is locked. Find the " + currentLevel.itemName + "!", 100);
            }
        }

        if (playerY > HEIGHT) {
            gameOver = true;
            setMessage("You fell! Press R to retry this level.", 999999);
        }
    }

    private void jump() {
        if (!gameOver && !gameWon && onGround) {
            velocityY = -12.5;
            onGround = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(g2);

        // Ground
        g2.setColor(currentLevel.ground);
        g2.fillRect(0, FLOOR, WIDTH, HEIGHT - FLOOR);
        g2.setColor(currentLevel.accent.darker());
        g2.fillRect(0, FLOOR, WIDTH, 5);
        for (int x = 0; x < WIDTH; x += 28) {
            g2.drawLine(x, FLOOR + 15, x + 12, FLOOR + 7);
        }

        for (Rectangle p : platforms) drawPlatform(g2, p);
        drawExit(g2);
        if (!itemCollected) drawCollectible(g2);
        for (Enemy enemy : enemies) {
            if (enemy.spider) drawSpider(g2, enemy);
            else drawScorpion(g2, enemy);
        }
        drawRanger(g2, playerX, playerY, facingRight);

        drawHUD(g2);
        if (messageTicks > 0 || gameOver || gameWon) {
            drawMessage(g2);
        }
        g2.dispose();
    }

    private void drawBackground(Graphics2D g2) {
        GradientPaint sky = new GradientPaint(0, 0, currentLevel.skyTop,
                0, HEIGHT, currentLevel.skyBottom);
        g2.setPaint(sky);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        if (level == 1) {
            g2.setColor(new Color(255, 240, 120));
            g2.fillOval(710, 35, 70, 70);
            g2.setColor(new Color(255, 255, 255, 210));
            drawCloud(g2, 100, 70);
            drawCloud(g2, 430, 105);
            drawCloud(g2, 640, 145);
        } else if (level == 2) {
            g2.setColor(new Color(220, 230, 245));
            g2.fillOval(690, 35, 60, 60);
            g2.setColor(new Color(90, 105, 130, 150));
            for (int i = 0; i < 45; i++) {
                int x = (i * 67 + animationTick * 3) % WIDTH;
                int y = (i * 43 + animationTick * 5) % FLOOR;
                g2.drawLine(x, y, x - 7, y + 15);
            }
        } else if (level == 3) {
            g2.setColor(new Color(255, 245, 180));
            g2.fillOval(690, 45, 64, 64);
            g2.setColor(new Color(190, 130, 65, 100));
            for (int i = 0; i < 8; i++) {
                int x = (i * 130 + animationTick) % (WIDTH + 150) - 100;
                g2.fillOval(x, 260 + (i % 3) * 30, 170, 24);
            }
            g2.setColor(new Color(215, 165, 80));
            g2.fillOval(-100, 330, 430, 130);
            g2.fillOval(300, 345, 420, 120);
            g2.fillOval(620, 320, 390, 150);
        } else if (level == 4) {
            g2.setColor(new Color(245, 245, 205));
            g2.fillOval(680, 35, 62, 62);
            g2.setColor(new Color(20, 50, 45, 130));
            for (int x = 25; x < WIDTH; x += 115) {
                g2.fillRect(x + 25, 180, 18, 260);
                g2.fillOval(x - 20, 130, 110, 110);
            }
            g2.setColor(new Color(120, 140, 180, 60));
            g2.fillRect(0, 300, WIDTH, 100);
        } else if (level == 5) {
            g2.setColor(Color.WHITE);
            g2.fillOval(680, 35, 65, 65);
            g2.setColor(new Color(255, 255, 255, 180));
            for (int i = 0; i < 70; i++) {
                int x = (i * 53 + animationTick) % WIDTH;
                int y = (i * 37 + animationTick * 2) % FLOOR;
                g2.fillOval(x, y, 3, 3);
            }
            g2.setColor(new Color(235, 248, 255));
            g2.fillOval(-80, 345, 400, 120);
            g2.fillOval(280, 360, 430, 110);
            g2.fillOval(610, 340, 400, 130);
        } else {
            g2.setColor(new Color(255, 180, 60));
            g2.fillOval(690, 60, 70, 70);
            g2.setColor(new Color(95, 35, 40));
            g2.fillPolygon(new int[]{0, 120, 250, 370, 510, 660, 800, 900},
                    new int[]{360, 210, 350, 185, 335, 200, 345, 250}, 8);
            g2.setColor(new Color(255, 100, 20, 190));
            for (int i = 0; i < 18; i++) {
                int x = (i * 59 + animationTick * 2) % WIDTH;
                int y = 220 + (i * 31 % 190);
                g2.fillOval(x, y, 4, 7);
            }
        }
    }

    private void drawCloud(Graphics2D g2, int x, int y) {
        g2.fillOval(x, y + 10, 65, 30);
        g2.fillOval(x + 18, y, 42, 40);
        g2.fillOval(x + 42, y + 9, 50, 31);
    }

    private void drawPlatform(Graphics2D g2, Rectangle p) {
        g2.setColor(currentLevel.platform);
        g2.fillRoundRect(p.x, p.y, p.width, p.height, 8, 8);
        g2.setColor(currentLevel.accent);
        g2.fillRoundRect(p.x, p.y, p.width, 5, 6, 6);
        g2.setColor(new Color(0, 0, 0, 45));
        g2.drawLine(p.x + 8, p.y + p.height - 3, p.x + p.width - 8, p.y + p.height - 3);
    }

    private void drawCollectible(Graphics2D g2) {
        int x = collectible.x;
        int y = collectible.y;
        g2.setColor(currentLevel.accent);
        g2.fillOval(x, y, collectible.width, collectible.height);
        g2.setColor(Color.WHITE);
        g2.drawOval(x + 4, y + 4, collectible.width - 8, collectible.height - 8);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(Color.BLACK);
        g2.drawString(level == 1 ? "$" : level == 2 ? "C" : level == 3 ? "R" :
                level == 4 ? "M" : level == 5 ? "*" : "F", x + 8, y + 18);
        g2.setColor(new Color(255, 255, 255, 90));
        g2.fillOval(x + 4, y + 3, 7, 7);
    }

    private void drawExit(Graphics2D g2) {
        g2.setColor(exitUnlocked ? new Color(50, 210, 100) : new Color(95, 65, 45));
        g2.fillRoundRect(exitDoor.x, exitDoor.y, exitDoor.width, exitDoor.height, 8, 8);
        g2.setColor(new Color(235, 220, 170));
        g2.drawRoundRect(exitDoor.x, exitDoor.y, exitDoor.width, exitDoor.height, 8, 8);
        g2.fillOval(exitDoor.x + 29, exitDoor.y + 38, 5, 5);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString(exitUnlocked ? "OPEN" : "LOCK", exitDoor.x - 2, exitDoor.y - 7);
    }

    // Original cartoon space explorer character, drawn with Java shapes only.
    private void drawRanger(Graphics2D g2, int x, int y, boolean right) {
        int cx = x + PLAYER_W / 2;
        int direction = right ? 1 : -1;

        // Boots
        g2.setColor(new Color(35, 45, 65));
        g2.fillRoundRect(x + (right ? 2 : 0), y + 39, 15, 7, 4, 4);
        g2.fillRoundRect(x + (right ? 18 : 16), y + 39, 15, 7, 4, 4);

        // Legs and utility suit
        g2.setColor(new Color(40, 145, 125));
        g2.fillRoundRect(x + 6, y + 28, 10, 14, 4, 4);
        g2.fillRoundRect(x + 19, y + 28, 10, 14, 4, 4);
        g2.setColor(new Color(25, 105, 100));
        g2.fillRoundRect(x + 4, y + 17, 27, 18, 6, 6);
        g2.setColor(new Color(245, 190, 65));
        g2.fillRoundRect(x + 5, y + 22, 25, 5, 3, 3);

        // Backpack
        g2.setColor(new Color(235, 125, 45));
        g2.fillRoundRect(x + (right ? 0 : 27), y + 19, 7, 15, 3, 3);

        // Face and ears
        g2.setColor(new Color(190, 125, 85));
        g2.fillOval(x + 7, y + 2, 21, 21);
        g2.fillOval(x + (right ? 25 : 3), y + 10, 5, 7);

        // Dark hair and explorer headband
        g2.setColor(new Color(45, 35, 30));
        g2.fillArc(x + 6, y, 23, 13, 0, 180);
        g2.setColor(new Color(245, 190, 65));
        g2.fillRoundRect(x + 5, y + 4, 24, 5, 4, 4);

        // Eye and small headset
        g2.setColor(Color.WHITE);
        g2.fillOval(cx + direction * 4 - 2, y + 9, 6, 6);
        g2.setColor(Color.BLACK);
        g2.fillOval(cx + direction * 5, y + 11, 3, 3);
        g2.setColor(new Color(65, 75, 85));
        g2.fillOval(x + (right ? 25 : 4), y + 14, 5, 5);

        // Suit badge and belt buckle
        g2.setColor(new Color(255, 235, 150));
        g2.fillOval(x + 11, y + 22, 5, 5);
        g2.setColor(new Color(230, 235, 240));
        g2.fillRect(x + 18, y + 30, 7, 3);
    }

    private void drawSpider(Graphics2D g2, Enemy e) {
        int x = (int) e.x;
        int y = e.y;
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(30, 25, 35));
        for (int i = 0; i < 4; i++) {
            int offset = i * 7;
            g2.drawLine(x + 8, y + 12, x - 4 - offset, y + 5 + i * 5);
            g2.drawLine(x + e.width - 8, y + 12, x + e.width + 4 + offset, y + 5 + i * 5);
        }
        g2.setColor(new Color(55, 35, 65));
        g2.fillOval(x + 4, y + 3, e.width - 8, e.height - 4);
        g2.setColor(Color.RED);
        g2.fillOval(x + 10, y + 9, 5, 5);
        g2.fillOval(x + 20, y + 9, 5, 5);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawScorpion(Graphics2D g2, Enemy e) {
        int x = (int) e.x;
        int y = e.y;
        g2.setColor(new Color(175, 75, 35));
        g2.fillOval(x + 5, y + 12, e.width - 10, e.height - 12);
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(x + 7, y + 17, x - 1, y + 23);
        g2.drawLine(x + 7, y + 21, x - 1, y + 26);
        g2.drawLine(x + e.width - 7, y + 15, x + e.width + 2, y + 7);
        g2.drawLine(x + e.width + 2, y + 7, x + e.width + 5, y + 1);
        g2.setColor(new Color(245, 185, 60));
        g2.fillOval(x + e.width + 2, y - 2, 7, 7);
        g2.setColor(Color.BLACK);
        g2.fillOval(x + 13, y + 14, 4, 4);
        g2.fillOval(x + 22, y + 14, 4, 4);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 145));
        g2.fillRoundRect(12, 12, 330, 78, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("COIN QUEST  |  LEVEL " + level + "/6", 25, 37);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString(currentLevel.name + " - " + currentLevel.weather, 25, 58);
        g2.drawString("Item: " + (itemCollected ? "Collected" : currentLevel.itemName), 25, 78);

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(WIDTH - 285, 12, 270, 48, 12, 12);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.drawString("Move: A/D or \u2190/\u2192    Jump: W/SPACE/\u2191", WIDTH - 275, 31);
        g2.drawString("Restart level: R", WIDTH - 275, 49);
    }

    private void drawMessage(Graphics2D g2) {
        int boxW = 560;
        int boxH = gameOver || gameWon ? 115 : 58;
        int boxX = (WIDTH - boxW) / 2;
        int boxY = 145;
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 18, 18);
        g2.setColor(currentLevel.accent);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 18, 18);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, gameOver || gameWon ? 19 : 16));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(message, boxX + (boxW - fm.stringWidth(message)) / 2, boxY + 33);
        if (gameOver || gameWon) {
            g2.setFont(new Font("Arial", Font.PLAIN, 15));
            String sub = gameWon ? "Congratulations! You are the Coin Quest champion." :
                    "Press R to try again. Avoid all spiders and scorpions!";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(sub, boxX + (boxW - fm2.stringWidth(sub)) / 2, boxY + 67);
            g2.drawString("Tip: Jump over enemies and collect the item before the exit.",
                    boxX + 42, boxY + 91);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) movingLeft = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) movingRight = true;
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP || key == KeyEvent.VK_SPACE) jump();
        if (key == KeyEvent.VK_R) {
            if (gameWon) loadLevel(1);
            else loadLevel(level);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) movingLeft = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) movingRight = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed.
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Coin Quest - 6 Level Platform Adventure");
            CrystalRunner game = new CrystalRunner();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
