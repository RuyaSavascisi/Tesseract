package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BlockEntityBaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.TesseractClient;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.packets.PacketScreenRemoveChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * Created 5/28/2021 by SuperMartijn642
 */
public class TesseractRemoveChannelScreen extends BlockEntityBaseWidget<TesseractBlockEntity> {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tesseract", "textures/gui/add_screen_background.png");

    private final EnumChannelType type;
    private final int channelId;

    protected TesseractRemoveChannelScreen(Level level, BlockPos pos, EnumChannelType type, int channelId){
        super(0, 0, 144, 65, level, pos);
        this.type = type;
        this.channelId = channelId;
    }

    @Override
    protected void addWidgets(TesseractBlockEntity entity){
        TesseractButton removeButton = this.addWidget(new TesseractButton(8, 43, 61, 14, TextComponents.translation("gui.tesseract.remove.remove").get(),
            () -> {
                Tesseract.CHANNEL.sendToServer(new PacketScreenRemoveChannel(this.type, this.channelId));
                TesseractClient.openScreen(this.blockEntityPos);
            }));
        removeButton.setRedBackground();
        this.addWidget(new TesseractButton(75, 43, 61, 14, TextComponents.translation("gui.tesseract.remove.cancel").get(),
            () -> TesseractClient.openScreen(this.blockEntityPos)));

        super.addWidgets(entity);
    }

    @Override
    protected void render(WidgetRenderContext context, int mouseX, int mouseY, TesseractBlockEntity entity){
        ScreenUtils.bindTexture(BACKGROUND);
        ScreenUtils.drawTexture(context.poseStack(), 0, 0, this.width(), this.height());

        Channel channel = TesseractChannelManager.CLIENT.getChannelById(this.type, this.channelId);
        if(channel == null){
            ClientUtils.closeScreen();
            return;
        }
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.translation("gui.tesseract.remove.title." + this.type.name().toLowerCase(Locale.ROOT)).get(), 72, 6, 0xffffffff);
        // Render player head and channel name
        int nameWidth = ClientUtils.getFontRenderer().width(channel.name);
        int x = 72 - (9 + 3 + nameWidth + 3 + 9) / 2;
        PlayerRenderer.renderPlayerHead(channel.creator, context.poseStack(), x, 24, 9, 9);
        ScreenUtils.drawString(context.poseStack(), channel.name, x + 12, 25, 0xffffffff);
        if(channel.creator.equals(ClientUtils.getPlayer().getUUID())){
            ScreenUtils.bindTexture(channel.isPrivate ? TesseractScreen.LOCK_ON : TesseractScreen.LOCK_OFF);
            ScreenUtils.drawTexture(context.poseStack(), x + 12 + nameWidth + 3, 24, 9, 9);
        }

        super.render(context, mouseX, mouseY, entity);
    }

    @Override
    protected Component getNarrationMessage(TesseractBlockEntity object){
        return TextComponents.translation("gui.tesseract.remove.title." + this.type.name().toLowerCase(Locale.ROOT)).get();
    }
}
