import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.geom.Rectangle2D
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer


class main() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            val allEntities = mutableListOf<Entity>()
            val entsToAdd = mutableListOf<Entity>()

            var dirup = false
            var dirdown = false
            var dirright = false
            var dirleft = false
            var spinright = false
            var spinleft = false
            var shooting = false

            val diesoffScreen = object : GameComponent {
                override fun Update(entity: Entity) {
                    if (entity.xpos > 450) {
                        entity.isDead = true
                    }
                }

                override fun DrawComponent(g: Graphics, entity: Entity) {}
            }

            fun diesFromCollide(withtag: String) = object : GameComponent {
                override fun Update(entity: Entity) {
                    allEntities.forEach {
                        if (it != entity && it.entityTag == withtag) {
                            if (it.getshape().intersects(entity.getshape() as Rectangle2D)) {
                                entity.isDead = true
                            }
                        }
                    }
                }

                override fun DrawComponent(g: Graphics, entity: Entity) {}
            }

            fun blockedby(withTag: List<String>) = object : GameComponent {
                override fun Update(entity: Entity) {
                    allEntities.forEach {
                        if (withTag.contains(it.entityTag)) {
                            if (it != entity) {
                                if (it.getshape().intersects(Rectangle(entity.xpos + 1, entity.ypos, 50, 50))) {
                                    entity.xpos--
                                }
                                if (it.getshape().intersects(Rectangle(entity.xpos - 1, entity.ypos, 50, 50))) {
                                    entity.xpos++
                                }
                                if (it.getshape().intersects(Rectangle(entity.xpos, entity.ypos + 1, 50, 50))) {
                                    entity.ypos--
                                }
                                if (it.getshape().intersects(Rectangle(entity.xpos - 1, entity.ypos - 1, 50, 50))) {
                                    entity.ypos++
                                }
                            }
                        }
                    }
                }

                override fun DrawComponent(g: Graphics, entity: Entity) {

                }
            }

            fun drifts(an: Int): GameComponent = object : GameComponent {
                override fun Update(entity: Entity) {
                    val realang = (an % 360) * Math.PI / 180
                    entity.ypos -= ((Math.sin(realang)) * 10).toInt()
                    entity.xpos += ((Math.cos(realang)) * 10).toInt()
                }

                override fun DrawComponent(g: Graphics, entity: Entity) {

                }
            }

            fun playerControllable(playerNum: Int) = object : GameComponent {
                override fun Update(entity: Entity) {
                    if (playerNum == 1) {
                        if (dirright && entity.xpos < 451) entity.xpos++
                        if (dirleft && entity.xpos > 0) entity.xpos--
                        if (dirup && entity.ypos > 0) entity.ypos--
                        if (dirdown && entity.ypos < 450) entity.ypos++
                    }
                }

                override fun DrawComponent(g: Graphics, entity: Entity) {}
            }

            fun CanShoot(bot: Boolean) = object : GameComponent {
                var angy = 0
                override fun Update(entity: Entity) {
                    var bulstring = "bullet"
                    if (!bot) {
                        if (spinleft) angy -= 3
                        if (spinright) angy += 3
                    } else {
                        angy += 4
                        bulstring = "botbullet"
                    }

                    if (shooting) {
                        var randnum = Math.random() * 10
                        var adjustedAngy = angy + (randnum.toInt() * 2)
                        entsToAdd.add(
                            object : Entity {
                                override var isDead = false
                                override val entityTag = bulstring
                                override var xpos = entity.xpos + 23
                                override var ypos = entity.ypos + 23
                                override val components = listOf<GameComponent>(diesoffScreen, drifts(adjustedAngy))
                                override fun draw(g: Graphics) {
                                    g.color = Color.BLUE
                                    g.fillRect(xpos, ypos, 5, 5)
                                }

                                override fun getshape(): Shape {
                                    return Rectangle(xpos, ypos, 5, 5)
                                }
                            })
                    }

                }

                override fun DrawComponent(g: Graphics, entity: Entity) {
                    g.color = Color.BLACK
                    (g as Graphics2D).stroke = BasicStroke(5f)
                    g.drawArc(entity.xpos - 4, entity.ypos - 5, 60, 60, angy - 5, 13)

                }
            }


            val movesTowardsPlayer = object : GameComponent {
                override fun Update(entity: Entity) {
                    if (allEntities.filter { it.entityTag == "player" }.first().xpos > entity.xpos) entity.xpos++
                    else entity.xpos--

                    if (allEntities.filter { it.entityTag == "player" }.first().ypos > entity.ypos) entity.ypos++
                    else entity.ypos--
                }

                override fun DrawComponent(g: Graphics, entity: Entity) {

                }
            }


            fun damagedBy(withTag: String, hp: Int) = object : GameComponent {
                var currentHp = hp
                val maxHP = hp
                override fun Update(entity: Entity) {
                    allEntities.forEach {
                        if (it != entity && it.entityTag == withTag) {
                            if (it.getshape().intersects(entity.getshape() as Rectangle2D)) {
                                currentHp--
                                it.isDead = true
                                if (currentHp < 1) entity.isDead = true
                            }
                        }
                    }
                }

                override fun DrawComponent(g: Graphics, entity: Entity) {

                    g.color = Color.BLUE
                    (g as Graphics2D).stroke = BasicStroke(2f)
                    g.drawLine(
                        entity.xpos,
                        entity.ypos - 10,
                        entity.xpos + (50 * (currentHp) / maxHP),
                        entity.ypos - 10

                    )
                    g.drawLine(
                        entity.xpos,
                        entity.ypos - 12,
                        entity.xpos + (50 * (currentHp) / maxHP),
                        entity.ypos - 12

                    )
                    (g as Graphics2D).stroke = BasicStroke(1f)
                }
            }


            val samEntity = object : Entity {
                override var isDead = false
                override val entityTag = "goblin"
                override var xpos = 50
                override var ypos = 50
                override val components =
                    listOf<GameComponent>(
                        blockedby(listOf("player")),
                        diesoffScreen,
                        movesTowardsPlayer,
                        damagedBy("bullet", 50),
                        CanShoot(true)
                    )

                override fun draw(g: Graphics) {
                    g.fillRect(xpos, ypos, 50, 50)
                }

                override fun getshape(): Shape {
                    return Rectangle(xpos, ypos, 50, 50)
                }
            }
            val jimEntity = object : Entity {
                override val entityTag = "player"
                override var isDead = false
                override var xpos = 200
                override var ypos = 100
                override val components = listOf(
                    playerControllable(1),
                    blockedby(listOf("wall", "goblin")),
                    CanShoot(false),
                    damagedBy("botbullet", 100)
                )

                override fun draw(g: Graphics) {
                    g.color = Color.PINK
                    g.fillOval(xpos, ypos, 50, 50)
                }

                override fun getshape(): Shape {
                    return Rectangle(xpos, ypos, 50, 50)
                }
            }
            val wall = object : Entity {
                override val entityTag = "wall"
                override var isDead = false
                override var xpos = 150
                override var ypos = 150
                override val components = listOf<GameComponent>(damagedBy("bullet", 100))
                override fun draw(g: Graphics) {
                    g.color = Color.GREEN
                    g.fillRect(xpos, ypos, 50, 50)
                }

                override fun getshape(): Shape {
                    return Rectangle(xpos, ypos, 50, 50)
                }
            }

            allEntities.add(samEntity)
            allEntities.add(jimEntity)
            allEntities.add(wall)

            val myFrame = JFrame()
            myFrame.title = "JimSamGame"
            myFrame.setBounds(0, 0, 500, 500)
            myFrame.isVisible = true

            val myPanel = object : JPanel() {
                override fun paint(g: Graphics) {
                    super.paint(g)

                    allEntities.removeIf { it.isDead }

                    allEntities.forEach { entity: Entity ->
                        entity.components.forEach {
                            it.Update(entity)
                        }
                    }

                    allEntities.addAll(entsToAdd)
                    entsToAdd.clear()

                    allEntities.forEach { entity ->
                        entity.draw(g)
                        entity.components.forEach {
                            it.DrawComponent(g, entity)
                        }
                    }
                }
            }
            myPanel.addKeyListener(
                object : KeyListener {
                    override fun keyTyped(e: KeyEvent?) {}
                    override fun keyPressed(e: KeyEvent) {
                        if (e.keyCode == KeyEvent.VK_RIGHT) dirright = true
                        if (e.keyCode == KeyEvent.VK_LEFT) dirleft = true
                        if (e.keyCode == KeyEvent.VK_UP) dirup = true
                        if (e.keyCode == KeyEvent.VK_DOWN) dirdown = true
                        if (e.keyCode == KeyEvent.VK_NUMPAD9) spinleft = true
                        if (e.keyCode == KeyEvent.VK_NUMPAD7) spinright = true
                        if (e.keyCode == KeyEvent.VK_NUMPAD5) shooting = true
                    }

                    override fun keyReleased(e: KeyEvent) {
                        if (e.keyCode == KeyEvent.VK_RIGHT) dirright = false
                        if (e.keyCode == KeyEvent.VK_LEFT) dirleft = false
                        if (e.keyCode == KeyEvent.VK_UP) dirup = false
                        if (e.keyCode == KeyEvent.VK_DOWN) dirdown = false
                        if (e.keyCode == KeyEvent.VK_NUMPAD9) spinleft = false
                        if (e.keyCode == KeyEvent.VK_NUMPAD7) spinright = false
                        if (e.keyCode == KeyEvent.VK_NUMPAD5) shooting = false
                    }
                })
            myPanel.isFocusable = true
            myPanel.focusTraversalKeysEnabled = true
            myPanel.background = Color.MAGENTA
            myFrame.add(myPanel)
            Timer(40, ActionListener { myPanel.repaint() }).start()
        }
    }
}

interface Entity {
    var xpos: Int
    var ypos: Int
    fun getshape(): Shape
    val components: List<GameComponent>
    var isDead: Boolean
    val entityTag: String
    fun draw(g: Graphics)

}

interface GameComponent {
    fun Update(entity: Entity)
    fun DrawComponent(g: Graphics, entity: Entity)
}