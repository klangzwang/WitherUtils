package geni.witherutils.common.world.item;

import geni.withercore.common.world.item.WitherItem;
import geni.witherutils.common.menu.CutterMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CutterItem extends WitherItem {

    public CutterItem(Item.Properties props)
    {
        super(props.stacksTo(1).durability(250));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn)
    {
        ItemStack stack = player.getItemInHand(handIn);
        if (player instanceof ServerPlayer sp)
        {
            openGui(sp, stack, handIn);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack remote, UseOnContext ctx)
    {
        Player player = ctx.getPlayer();
        if (player instanceof ServerPlayer sp)
            openGui(sp, remote, ctx.getHand());
        return InteractionResult.SUCCESS;
    }

    private void openGui(ServerPlayer player, ItemStack remote, InteractionHand hand)
    {
        if (!player.isCrouching())
            player.openMenu(new CutterMenuProvider());
    }

    @Override
    public int getEnchantmentValue(ItemStack stack)
    {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack par1ItemStack, BlockState par2Block)
    {
        return 1F;
    }

    @Override
    public int getMaxDamage(ItemStack stack)
    {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack damagedItem, ItemStack repairMaterial)
    {
        return repairMaterial == Items.IRON_INGOT.getDefaultInstance();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }
}

class CutterMenuProvider implements MenuProvider {

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("cutter");
    }
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player)
    {
        return new CutterMenu(i, playerInventory, ContainerLevelAccess.create(player.level(), player.blockPosition()));
    }
}
