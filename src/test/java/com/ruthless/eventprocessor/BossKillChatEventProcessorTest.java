package com.ruthless.eventprocessor;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.ruthless.RuthlessConfig;
import com.ruthless.RuthlessPlugin;
import com.ruthless.ui.overlay.EventCodewordOverlay;
import com.ruthless.web.RuthlessClient;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import com.google.inject.testing.fieldbinder.Bind;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class BossKillChatEventProcessorTest {

    @Bind
    @InjectMocks
    BossKillChatEventProcessor processor;

    @Bind
    protected Client client = Mockito.mock(Client.class);

    @Bind
    protected RuthlessClient rc = Mockito.mock(RuthlessClient.class);

    @Bind
    protected RuthlessConfig config = Mockito.mock(RuthlessConfig.class);

    @Bind
    protected RuthlessPlugin plugin = Mockito.spy(RuthlessPlugin.class);

    @Bind
    protected ClanMemberProcessor cmp = Mockito.mock(ClanMemberProcessor.class);

    @Bind
    protected InfoBoxManager infoBoxManager = Mockito.mock(InfoBoxManager.class);

    @Bind
    protected OverlayManager overlayManager = Mockito.mock(OverlayManager.class);

    @Bind
    protected LootReceivedProcessor lootReceivedProcessor = Mockito.mock(LootReceivedProcessor.class);

    @Bind
    protected ConfigManager configManager = Mockito.mock(ConfigManager.class);

    @Bind
    protected ItemManager itemManager = Mockito.mock(ItemManager.class);

    @Bind
    protected ChatMessageManager chatMessageManager = Mockito.mock(ChatMessageManager.class);

    @Bind
    protected EventCodewordOverlay eventCodewordOverlay = Mockito.mock(EventCodewordOverlay.class);

    @Bind
    protected Player localPlayer = Mockito.mock(Player.class);

    private AutoCloseable mocks;

    @BeforeEach
    protected void setUp() {
        this.mocks = MockitoAnnotations.openMocks(this);
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
        Player player = Mockito.mock(Player.class);
        when(localPlayer.getName()).thenReturn("RuthlessPvmR");
        when(client.getLocalPlayer()).thenReturn(localPlayer);
        when(client.getWorld()).thenReturn(479);
    }

    @AfterEach
    protected void cleanUp() throws Exception {
        mocks.close();
    }

    @Test
    public void testNotifyHydra() {
        String message = "Your Alchemical Hydra kill count is: <col=ff0000>1000</col>";
        ChatMessage msg = new ChatMessage();
        msg.setType(ChatMessageType.GAMEMESSAGE);
        msg.setMessage(message);

        processor.onChatMessage(msg);

        String message2 = "Fight duration: <col=ff0000>2:28</col>. Personal best: 2:08.";
        ChatMessage msg2 = new ChatMessage();
        msg2.setType(ChatMessageType.GAMEMESSAGE);
        msg2.setMessage(message2);

        processor.onChatMessage(msg2);

        Mockito.verify(rc).submitBossTimeRequest(Mockito.any());
    }

    @Test
    public void testNotifyColo() {
        String message = "Your Sol Heredit kill count is: <col=ff0000>4</col>.";
        ChatMessage msg = new ChatMessage();
        msg.setType(ChatMessageType.GAMEMESSAGE);
        msg.setMessage(message);

        processor.onChatMessage(msg);

        String message2 = "Colosseum duration: @mes_hl_red@23:13.80</col>. Personal best: 22:37.20";
        ChatMessage msg2 = new ChatMessage();
        msg2.setType(ChatMessageType.GAMEMESSAGE);
        msg2.setMessage(message2);

        processor.onChatMessage(msg2);

        Mockito.verify(rc).submitBossTimeRequest(Mockito.any());
    }
}
