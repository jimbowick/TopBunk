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
            var dirup = false
            var dirdown = false
            var dirright = false
            var dirleft = false
            var playerAngle = 0
            var spinright = false
            var spinleft = false
            var shooting = false


            val rises = object : GameComponent {
                override fun Update(entity: Entity) {
                    entity.ypos--
                }
            }

            val diesoffScreen = object : GameComponent {
                override fun Update(entity: Entity) {
                    if (entity.xpos > 450) {
                        entity.isDead = true
                    }
                }
            }

            val diesFromCollide = object : GameComponent {
                override fun Update(entity: Entity) {
                    allEntities.forEach {
                        if (it != entity) {
                            if (it.getshape().intersects(entity.getshape() as Rectangle2D)) {
                                entity.isDead = true
                            }
                        }
                    }
                }
            }
            val blockedbywall = object : GameComponent {
                override fun Update(entity: Entity) {
                    allEntities.forEach {
                        if (it.entityTag == "wall") {
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
            }
            val entsToAdd = mutableListOf<Entity>()

            fun drifts(an:Int):GameComponent = object :GameComponent{
                override fun Update(entity: Entity) {
                    val realang = (an%360) * Math.PI/180
                    entity.ypos-=((Math.sin(realang))*10).toInt()
                    entity.xpos+=((Math.cos(realang))*10).toInt()
                }
            }

            val CanShoot = object: GameComponent{
                override fun Update(entity: Entity) {
                    if(shooting){
                        val ang = playerAngle
                        entsToAdd.add(
                            object :Entity{
                                override var isDead = false
                                override val entityTag = "bullet"
                                override var xpos = entity.xpos+23
                                override var ypos = entity.ypos+23
                                override val components = listOf<GameComponent>(diesoffScreen,drifts(ang))
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
            }

            val playerControllable = object : GameComponent {
                override fun Update(entity: Entity) {
                    if (dirright && entity.xpos < 451) entity.xpos++
                    if (dirleft && entity.xpos > 0) entity.xpos--
                    if (dirup && entity.ypos > 0) entity.ypos--
                    if (dirdown && entity.ypos < 450) entity.ypos++
                    if (spinleft) playerAngle-=3
                    if (spinright) playerAngle+=3
                }
            }

            val samEntity = object : Entity {
                override var isDead = false
                override val entityTag = "goblin"
                override var xpos = 50
                override var ypos = 50
                override val components = listOf<GameComponent>(diesFromCollide, diesoffScreen)
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
                override val components = listOf<GameComponent>(playerControllable, blockedbywall,CanShoot)
                override fun draw(g: Graphics) {

                    g.fillOval(xpos, ypos, 50, 50)
                    g.drawRect(xpos,ypos, 50, 50)
                    g.color = Color.BLACK
                    g.drawArc(xpos -5, ypos -5, 60, 60, playerAngle-10, 20)

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
                override val components = listOf<GameComponent>(diesFromCollide)
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

                    g.color = Color.LIGHT_GRAY
                    g.fillRect(0, 0, 500, 500)
                    g.color = Color.GREEN

                    allEntities.removeIf { it.isDead }

                    allEntities.forEach { entity: Entity ->
                        entity.components.forEach {
                            it.Update(entity)
                        }
                    }

                    allEntities.addAll(entsToAdd)
                    entsToAdd.clear()

                    allEntities.forEach {
                        it.draw(g)
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
                        if (e.keyCode == KeyEvent.VK_NUMPAD7) spinleft = true
                        if (e.keyCode == KeyEvent.VK_NUMPAD9) spinright = true
                        if (e.keyCode == KeyEvent.VK_NUMPAD5) shooting = true


                    }

                    override fun keyReleased(e: KeyEvent) {
                        if (e.keyCode == KeyEvent.VK_RIGHT) dirright = false
                        if (e.keyCode == KeyEvent.VK_LEFT) dirleft = false
                        if (e.keyCode == KeyEvent.VK_UP) dirup = false
                        if (e.keyCode == KeyEvent.VK_DOWN) dirdown = false
                        if (e.keyCode == KeyEvent.VK_NUMPAD7) spinleft = false
                        if (e.keyCode == KeyEvent.VK_NUMPAD9) spinright = false
                        if (e.keyCode == KeyEvent.VK_NUMPAD5) shooting = false

                    }
                })
            myPanel.isFocusable = true
            myPanel.focusTraversalKeysEnabled = true
            myFrame.add(myPanel)

            Timer(20, ActionListener
            { myPanel.repaint() }).start()
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
}