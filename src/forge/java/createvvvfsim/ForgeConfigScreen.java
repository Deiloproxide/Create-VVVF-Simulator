package createvvvfsim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public class ForgeConfigScreen extends Screen{
    private static final Component[] texts={
            Component.literal("为什么Forge不提供自动配置页面的API啊啊啊啊啊"),
            Component.literal("不想写配置页面GUI，太麻烦，摆烂了(╯°□°）╯┻━┻"),
            Component.literal("您有3种方式可以访问本模组配置："),
            Component.literal("1.通过Neoforge的版本访问此页面（有汉化）"),
            Component.literal("2.通过机械动力访问其他模组配置（无汉化）"),
            Component.literal("3.直接通过版本文件夹访问配置文件")};
    public ForgeConfigScreen(Minecraft client,Screen parent){
        super(texts[0]);
    }
    @Override
    public void init(){
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),button->onClose())
                .bounds(width/2-100,height/2+60,200,20).build());
    }
    @Override
    public void render(GuiGraphics gui,int mouse_x,int mouse_y,float partial_tick){
        renderBackground(gui);
        int x=width/2,y=height/2-70;
        for(Component text:texts){
            gui.drawCenteredString(font,text,x,y,0xffffff);
            y+=20;
        }
        super.render(gui,mouse_x,mouse_y,partial_tick);
    }
}