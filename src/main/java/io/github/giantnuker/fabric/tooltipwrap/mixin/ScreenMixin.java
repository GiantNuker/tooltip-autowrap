package io.github.giantnuker.fabric.tooltipwrap.mixin;

import io.github.giantnuker.fabric.tooltipwrap.TooltipDataHolder;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author Indigo A.
 */
@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    public TextRenderer font;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow protected ItemRenderer itemRenderer;

    @Inject(method = "renderTooltip(Ljava/util/List;II)V", at = @At("HEAD"))
    public void readInitialVariables(List<String> strings, int x, int y, CallbackInfo ci) {
        TooltipDataHolder.initX = x;
        TooltipDataHolder.initY = y;
        TooltipDataHolder.strings = strings;
        TooltipDataHolder.font = font;
        TooltipDataHolder.screenWidth = width;
        TooltipDataHolder.screenHeight = height;
        TooltipDataHolder.chopMap.clear();
        TooltipDataHolder.newHeight = -1;
    }
    @ModifyVariable(method = "renderTooltip(Ljava/util/List;II)V", ordinal = 3, at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/item/ItemRenderer;zOffset:F", ordinal = 0))
    public int readXPosition(int x) {
        TooltipDataHolder.x = x;
        return x;
    }
    /*@ModifyVariable(method = "renderTooltip(Ljava/util/List;II)V", ordinal = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;fillGradient(IIIIII)V", ordinal = 0))
    public int readYPosition(int y) {
        TooltipDataHolder.y = y;
        return y;
    }

    */
    @ModifyVariable(method = "renderTooltip(Ljava/util/List;II)V", ordinal = 3, at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/item/ItemRenderer;zOffset:F", ordinal = 0))
    public int fixXPosition(int x) {
        return Math.max(x, 5);
    }
    @ModifyVariable(method = "renderTooltip(Ljava/util/List;II)V", ordinal = 4, at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/item/ItemRenderer;zOffset:F", ordinal = 0))
    public int fixYPosition(int y) {
        int newY = TooltipDataHolder.x < 0 ? y + 24 : y;
        if (TooltipDataHolder.getNewHeight() + newY + 5 > height && y - TooltipDataHolder.getNewHeight() > 5) {
            newY = y - TooltipDataHolder.getNewHeight();
        }
        TooltipDataHolder.y = newY;
        TooltipDataHolder.txtY = newY - 10;
        return newY;
    }
    @ModifyVariable(method = "renderTooltip(Ljava/util/List;II)V", ordinal = 5, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;fillGradient(IIIIII)V", ordinal = 0))
    public int fixWidth(int width) {
        return TooltipDataHolder.x < 0 && TooltipDataHolder.longestStringWidth() > TooltipDataHolder.screenWidth - 10 ? TooltipDataHolder.screenWidth - 10 : width;
    }
    @ModifyVariable(method = "renderTooltip(Ljava/util/List;II)V", ordinal = 6, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;fillGradient(IIIIII)V", ordinal = 0))
    public int fixHeight(int height) {
        return TooltipDataHolder.getNewHeight();
    }
    @Redirect(method = "renderTooltip(Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLnet/minecraft/client/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I"))
    public int drawWrapped(TextRenderer textRenderer, String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int backgroundColor, int light) {
        if (TooltipDataHolder.isOffender(text)) {
            int yp = TooltipDataHolder.txtY;
            for (String chop: TooltipDataHolder.chop(text)) {
                font.draw(chop, x, yp += 10, color, shadow, matrix, vertexConsumerProvider, seeThrough, backgroundColor, light);
            }
            TooltipDataHolder.txtY = yp;
            return 0;
        } else {
            return font.draw(text, x, TooltipDataHolder.txtY += 10, color, shadow, matrix, vertexConsumerProvider, seeThrough, backgroundColor, light);
        }
    }
    /*@Overwrite
    public void wrapTooltip(List<String> list_1, int int_1, int int_2) {
        if (!list_1.isEmpty()) {
            GlStateManager.disableRescaleNormal();
            GuiLighting.disable();
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            int int_3 = 0;
            Iterator var5 = list_1.iterator();

            while(var5.hasNext()) {
                String string_1 = (String)var5.next();
                int int_4 = this.font.getStringWidth(string_1);
                if (int_4 > int_3) {
                    int_3 = int_4;
                }
            }

            int int_5 = int_1 + 12;
            int int_6 = int_2 - 12;
            int int_8 = 8;
            if (list_1.size() > 1) {
                int_8 += 2 + (list_1.size() - 1) * 10;
            }

            if (int_5 + int_3 > this.width) {
                int_5 -= 28 + int_3;
            }

            if (int_6 + int_8 + 6 > this.height) {
                int_6 = this.height - int_8 - 6;
            }

            this.blitOffset = 300;
            this.itemRenderer.zOffset = 300.0F;
            int int_9 = -267386864;
            this.fillGradient(int_5 - 3, int_6 - 4, int_5 + int_3 + 3, int_6 - 3, -267386864, -267386864);
            this.fillGradient(int_5 - 3, int_6 + int_8 + 3, int_5 + int_3 + 3, int_6 + int_8 + 4, -267386864, -267386864);
            this.fillGradient(int_5 - 3, int_6 - 3, int_5 + int_3 + 3, int_6 + int_8 + 3, -267386864, -267386864);
            this.fillGradient(int_5 - 4, int_6 - 3, int_5 - 3, int_6 + int_8 + 3, -267386864, -267386864);
            this.fillGradient(int_5 + int_3 + 3, int_6 - 3, int_5 + int_3 + 4, int_6 + int_8 + 3, -267386864, -267386864);
            int int_10 = 1347420415;
            int int_11 = 1344798847;
            this.fillGradient(int_5 - 3, int_6 - 3 + 1, int_5 - 3 + 1, int_6 + int_8 + 3 - 1, 1347420415, 1344798847);
            this.fillGradient(int_5 + int_3 + 2, int_6 - 3 + 1, int_5 + int_3 + 3, int_6 + int_8 + 3 - 1, 1347420415, 1344798847);
            this.fillGradient(int_5 - 3, int_6 - 3, int_5 + int_3 + 3, int_6 - 3 + 1, 1347420415, 1347420415);
            this.fillGradient(int_5 - 3, int_6 + int_8 + 2, int_5 + int_3 + 3, int_6 + int_8 + 3, 1344798847, 1344798847);

            for(int int_12 = 0; int_12 < list_1.size(); ++int_12) {
                String string_2 = (String)list_1.get(int_12);
                this.font.drawWithShadow(string_2, (float)int_5, (float)int_6, -1);
                if (int_12 == 0) {
                    int_6 += 2;
                }

                int_6 += 10;
            }

            this.blitOffset = 0;
            this.itemRenderer.zOffset = 0.0F;
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
            GuiLighting.enable();
            GlStateManager.enableRescaleNormal();
        }
    }*/
}
