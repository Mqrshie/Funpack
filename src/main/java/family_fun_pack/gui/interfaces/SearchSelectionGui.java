package family_fun_pack.gui.interfaces;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.lwjgl.input.Mouse;

import family_fun_pack.gui.MainGui;
import family_fun_pack.gui.components.ColorButton;
import family_fun_pack.gui.components.OnOffButton;
import family_fun_pack.gui.components.ScrollBar;
import family_fun_pack.gui.components.actions.OnOffSearch;
import family_fun_pack.gui.components.actions.OnOffTracer;
import family_fun_pack.modules.Module;
import family_fun_pack.modules.SearchModule;

@SideOnly(Side.CLIENT)
public class SearchSelectionGui extends RightPanel {

  private static final int INNER_BORDER = 0xffeeeeee;

  private static final int guiWidth = 268;
  private static final int guiHeight = 200;

  private static final int maxLabelsDisplayed = 10;

  private ScrollBar scroll;
  private GuiTextField selection;
  private String last_search;

  private int x, y, x_end, y_end;

  private List<Block> blocks;
  private List<OnOffButton> tracers;
  private List<OnOffButton> search;
  private List<ColorButton> colors;

  public SearchSelectionGui() {
    this.x = MainGui.guiWidth + 16;
    this.y = 12;
    this.x_end = SearchSelectionGui.guiWidth + this.x;
    this.y_end = SearchSelectionGui.guiHeight + this.y;

    this.blocks = new ArrayList<Block>();
    for(Block b : Block.REGISTRY) {
      this.blocks.add(b);
    }

    int max_scroll = this.blocks.size() - SearchSelectionGui.maxLabelsDisplayed;
    if(max_scroll < 0) max_scroll = 0;
    this.scroll = new ScrollBar(0, this.x_end - 10, this.y + 4, max_scroll, this.y_end - 4);
    this.buttonList.add(this.scroll);

    this.selection = new GuiTextField(0, this.fontRenderer, this.x + 4, this.y + 5, (int)((float)(this.x_end - 6 - this.scroll.width - this.x - 4) * 0.571f) - 6, 10);
    this.selection.setFocused(true);
    this.selection.setCanLoseFocus(false);
    this.selection.setMaxStringLength(256);

    this.last_search = "";
  }

  public void dependsOn(Module dependence) {
    super.dependsOn(dependence);

    this.tracers = new ArrayList<OnOffButton>(this.blocks.size());
    this.search = new ArrayList<OnOffButton>(this.blocks.size());
    this.colors = new ArrayList<ColorButton>(this.blocks.size());

    int chart_width = this.x_end - 6 - this.scroll.width - this.x - 4;

    for(int i = 0; i < this.blocks.size(); i ++) {

      int block_id = Block.getIdFromBlock(this.blocks.get(i));
      boolean search_state = ((SearchModule) this.dependence).getSearchState(block_id);

      OnOffButton tracer = new OnOffButton(i, 0, 0, new OnOffTracer(block_id, (SearchModule) this.dependence));
      tracer.x = ((((int)((float)chart_width * 0.143f)) - tracer.width) / 2) + (int)((float)chart_width * 0.714f) + this.x + 4;
      tracer.setState(((SearchModule) this.dependence).getTracerState(block_id));
      if(! search_state) tracer.enabled = false;

      ColorButton color = new ColorButton(0, 0, block_id, (SearchModule) this.dependence);
      color.x = ((((int)((float)chart_width * 0.143f)) - color.width) / 2) + (int)((float)chart_width * 0.857f) + this.x + 4;
      color.setColor(((SearchModule) this.dependence).getColor(block_id));
      if(! search_state) color.enabled = false;

      OnOffButton search = new OnOffButton(i, 0, 0, new OnOffSearch(block_id, (SearchModule) this.dependence, tracer, color));
      search.x = ((((int)((float)chart_width * 0.143f)) - search.width) / 2) + (int)((float)chart_width * 0.571f) + this.x + 4;
      search.setState(search_state);

      this.tracers.add(tracer);
      this.search.add(search);
      this.colors.add(color);
    }
  }

  private void searchBlocks(String keyword) {
    this.blocks.clear();
    for(Block b : Block.REGISTRY) {
      String label = Block.REGISTRY.getNameForObject(b).getResourcePath().replace("_", " ").toLowerCase();
      if(label.contains(keyword)) this.blocks.add(b);
    }
    int max_scroll = this.blocks.size() - SearchSelectionGui.maxLabelsDisplayed;
    if(max_scroll < 0) max_scroll = 0;
    this.scroll.resetMaxScroll(max_scroll);
    this.dependsOn(this.dependence);
  }

  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    Gui.drawRect(this.x, this.y, this.x_end, this.y_end, MainGui.BACKGROUND_COLOR); // GUI background

    // borders
    Gui.drawRect(this.x, this.y, this.x_end, this.y + 2, 0xffbbbbbb);
    Gui.drawRect(this.x, this.y, this.x + 2, this.y_end, 0xffbbbbbb);
    Gui.drawRect(this.x_end - 2, this.y, this.x_end, this.y_end, 0xffbbbbbb);
    Gui.drawRect(this.x, this.y_end - 2, this.x_end, this.y_end, 0xffbbbbbb);

    // Update scroll
    if(this.scroll.clicked) {
      this.scroll.dragged(mouseX, mouseY);
    }

    // Draw buttons
    super.drawScreen(mouseX, mouseY, partialTicks);

    // search bar
    this.selection.drawTextBox();

    // Draw titles
    int chart_end = this.x_end - 6 - this.scroll.width;
    int chart_width = chart_end - this.x - 4;
    GlStateManager.pushMatrix();
    GlStateManager.scale(0.9f, 0.9f, 0.9f);
    String[] labels = {"Search", "Tracer", "Color"};
    int x_total = this.x + 10 + this.selection.width;
    int decal_y = (int)((float)(this.y + 8) / 0.9f);
    int decal_x;
    for(String i : labels) {
      int width = (int)((float)chart_width * 0.143f);
      int str_width = this.fontRenderer.getStringWidth(i);
      int x = x_total + ((width - str_width) / 2);
      decal_x = (int)((float)(x) / 0.9f);
      this.drawString(this.fontRenderer, i, decal_x, decal_y, 0xffffffff);
      x_total += width;
    }
    GlStateManager.popMatrix();

    // Draw chart
    int scroll_end = this.scroll.current_scroll + SearchSelectionGui.maxLabelsDisplayed > this.blocks.size() ? this.blocks.size() : this.scroll.current_scroll + SearchSelectionGui.maxLabelsDisplayed;
    int y = 20 + this.y;
    Gui.drawRect(this.x + 4, y - 1, chart_end, y, SearchSelectionGui.INNER_BORDER);
    for(int i = this.scroll.current_scroll; i < scroll_end; i ++) {
      // Draw block
      this.displayBlockFlat(this.x + 4, y, this.blocks.get(i));

      // Draw label
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.7f, 0.7f, 0.7f);
      decal_y = (int)((float)(y + 4) / 0.7f);
      decal_x = (int)((float)(this.x + 22) / 0.7f);
      String label = Block.REGISTRY.getNameForObject(this.blocks.get(i)).getResourcePath().replace("_", " ");
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawString(this.fontRenderer, label, decal_x, decal_y, 0xffeeeeee);
      GlStateManager.popMatrix();

      // Draw buttons
      OnOffButton search = this.search.get(i);
      search.y = y + 4;
      search.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);

      OnOffButton tracer = this.tracers.get(i);
      tracer.y = y + 4;
      tracer.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);

      ColorButton color = this.colors.get(i);
      color.y = y + 4;
      color.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);

      // Draw border
      Gui.drawRect(this.x + 4, y + 16, chart_end, y + 17, SearchSelectionGui.INNER_BORDER);
      y += 17;
    }

    // Vertical borders
    Gui.drawRect(this.x + 3, this.y + 16, this.x + 4, this.y + 192, SearchSelectionGui.INNER_BORDER);
    Gui.drawRect(this.x + 20, this.y + 16, this.x + 21, this.y + 192, SearchSelectionGui.INNER_BORDER);
    Gui.drawRect(chart_end - 1, this.y + 16, chart_end, this.y + 192, SearchSelectionGui.INNER_BORDER);
    decal_x = this.x + 4 + (int)((float)chart_width * 0.571f);
    Gui.drawRect(decal_x - 1, this.y + 16, decal_x, this.y + 192, SearchSelectionGui.INNER_BORDER);
    decal_x = this.x + 4 + (int)((float)chart_width * 0.714f);
    Gui.drawRect(decal_x - 1, this.y + 16, decal_x, this.y + 192, SearchSelectionGui.INNER_BORDER);
    decal_x = this.x + 4 + (int)((float)chart_width * 0.857f);
    Gui.drawRect(decal_x - 1, this.y + 16, decal_x, this.y + 192, SearchSelectionGui.INNER_BORDER);
  }

  public void displayBlockFlat(int x, int y, Block block) {
    IBlockState state = block.getDefaultState();
    if(state.getRenderType() == EnumBlockRenderType.INVISIBLE) return;

    Minecraft mc = Minecraft.getMinecraft();
    ItemStack stack = new ItemStack(block);

    IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
    if(model == null || model == mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel()) {
      //model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(new ModelResourceLocation(Block.REGISTRY.getNameForObject(block).getResourcePath(), "inventory"));
      model = this.itemRender.getItemModelWithOverrides(stack, null, mc.player);
    }

    GlStateManager.pushMatrix();
    mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    mc.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableAlpha();
    GlStateManager.alphaFunc(516, 0.1F);
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    RenderHelper.enableGUIStandardItemLighting();

    GlStateManager.translate(x, y, 150.0F);
    GlStateManager.translate(8.0F, 8.0F, 0.0F);
    GlStateManager.scale(1.0F, -1.0F, 1.0F);
    GlStateManager.scale(16.0F, 16.0F, 16.0F);

    GlStateManager.disableLighting();

    GlStateManager.pushMatrix();
    GlStateManager.translate(-0.5F, -0.5F, -0.5F);

    if(model.isBuiltInRenderer()) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableRescaleNormal();
      stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
    } else {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
      for (EnumFacing enumfacing : EnumFacing.values())
      {
        this.itemRender.renderQuads(bufferbuilder, model.getQuads(state, enumfacing, 4242L), -1, stack);
      }
      this.itemRender.renderQuads(bufferbuilder, model.getQuads(state, null, 4242L), -1, stack);
      tessellator.draw();
    }

    GlStateManager.popMatrix();

    GlStateManager.disableAlpha();
    GlStateManager.disableRescaleNormal();
    GlStateManager.disableLighting();
    GlStateManager.popMatrix();
    mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    mc.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
  }

  public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    if(mouseButton == 0) {
      for(int i = this.scroll.current_scroll; (i - this.scroll.current_scroll) < SearchSelectionGui.maxLabelsDisplayed && i < this.blocks.size(); i ++) {
        OnOffButton search = this.search.get(i);
        if(search.mousePressed(this.mc, mouseX, mouseY)) {
          search.onClick(this);
          search.playPressSound(this.mc.getSoundHandler());
          return;
        }

        OnOffButton tracer = this.tracers.get(i);
        if(tracer.mousePressed(this.mc, mouseX, mouseY)) {
          tracer.onClick(this);
          tracer.playPressSound(this.mc.getSoundHandler());
          return;
        }

        this.colors.get(i).mousePressed(this.mc, mouseX, mouseY);
      }
      this.selection.mouseClicked(mouseX, mouseY, mouseButton);
      super.mouseClicked(mouseX, mouseY, mouseButton);
    }
  }

  public void mouseReleased(int mouseX, int mouseY, int state) {
    if(state == 0) {
      this.scroll.mouseReleased(mouseX, mouseY);

      for(int i = this.scroll.current_scroll; (i - this.scroll.current_scroll) < SearchSelectionGui.maxLabelsDisplayed && i < this.blocks.size(); i ++) {
        this.colors.get(i).mouseReleased(mouseX, mouseY);
      }
    }
  }

  public void updateScreen() {
    this.selection.updateCursorCounter();
  }

  public void keyTyped(char keyChar, int keyCode) throws IOException {
    this.selection.textboxKeyTyped(keyChar, keyCode);
    String keyword = this.selection.getText().trim().toLowerCase();
    if(! this.last_search.equals(keyword)) {
      this.last_search = keyword;
      this.searchBlocks(keyword);
    }
  }
}
