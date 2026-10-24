package pl.d2port;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import pl.d2port.d2.D2Native;

/**
 * The inventory panel as D2Client draws it in 800x600: invchar6.dc6 (four pieces, 2x2) in the panel
 * rectangle from inventory.txt, items at their grid cells or centred in their body-location boxes.
 * The 800x600 screen is scaled to the view's height and centred.
 */
final class InventoryView extends View {
    private static final String PANEL = "DATA\\GLOBAL\\UI\\PANEL\\INVCHAR6.DC6";
    // inventory.txt body components, in the order of D2InventoryComponent, by item body location.
    // Body locations: 1 head, 2 neck, 3 torso, 4 right arm, 5 left arm, 6 right ring, 7 left ring,
    // 8 belt, 9 feet, 10 gloves. Components: 0 rarm, 1 torso, 2 larm, 3 head, 4 neck, 5 rhand,
    // 6 lhand, 7 belt, 8 feet, 9 gloves.
    private static final int[] COMPONENT_OF_BODYLOC = {-1, 3, 4, 1, 0, 2, 5, 6, 7, 8, 9};

    private final Paint paint = new Paint();
    private Bitmap[] panel;
    private int[] panelOff;
    /** Set on the native thread by {@link #update}. */
    private volatile Object[] state;
    private final java.util.Map<String, Bitmap> graphics = new java.util.HashMap<>();

    InventoryView(Context c) { super(c); paint.setFilterBitmap(false); }

    // Vendor mode: the store (buysell.dc6 in the inventory.txt rectangle of the NPC's record) is
    // shown next to the hero's inventory; a tap buys from it or sells to it.
    private static final String STORE_PANEL = "DATA\\GLOBAL\\UI\\PANEL\\BUYSELL.DC6";
    /** Guid of the vendor being traded with, or -1. Set from the UI thread. */
    volatile int vendor = -1;
    private Bitmap[] storePanel;
    private volatile Object[] storeState;
    /** Store page shown (items' page value); tapping the top of the store panel moves to the next one. */
    private volatile int storePage = -1;

    private Object[] load(Object[] inv) {
        String[] files = (String[]) inv[1];
        Bitmap[] bitmaps = new Bitmap[files.length];
        for (int i = 0; i < files.length; i++) {
            Bitmap b = graphics.get(files[i]);
            if (b == null && !graphics.containsKey(files[i])) {
                Bitmap[] f = Dc6.frames(files[i]);
                b = f != null && f.length > 0 ? f[0] : null;
                graphics.put(files[i], b);
            }
            bitmaps[i] = b;
        }
        return new Object[]{inv[0], bitmaps, inv[2]};
    }

    /** Native thread: reads the hero's inventory and loads graphics not seen yet. */
    void update() {
        if (panel == null) {
            Bitmap[] p = Dc6.frames(PANEL);
            if (p != null) { panelOff = Dc6.lastOffsets; panel = p; }
        }
        Object[] inv = D2Native.inventory(-1);
        if (inv == null) return;
        state = load(inv);
        int v = vendor;
        Object[] store = v >= 0 ? D2Native.inventory(v) : null;
        if (store != null && storePanel == null) storePanel = Dc6.frames(STORE_PANEL);
        // NPCs without goods (Kashya, Warriv...) show no store.
        storeState = store != null && ((int[]) store[2]).length > 0 ? load(store) : null;
        if (store != null && storePage < 0) {
            int[] d = (int[]) store[2];
            if (d.length >= N) storePage = d[0];
        }
        postInvalidate();
    }

    @Override protected void onDraw(Canvas c) {
        Object[] s = state;
        if (s == null) return;
        int[] l = (int[]) s[0];
        Bitmap[] items = (Bitmap[]) s[1];
        int[] d = (int[]) s[2];
        float scale = getHeight() / 600f;
        c.save();
        c.translate((getWidth() - 800 * scale) / 2f, 0);
        c.scale(scale, scale);
        int left = l[0], top = l[2];
        Bitmap[] p = panel;
        // Frames 0-3 are the character sheet; this file's frames 4-7 are the inventory panel.
        if (p != null && p.length >= 8) {
            c.drawBitmap(p[4], left, top, paint);
            c.drawBitmap(p[5], left + p[4].getWidth(), top, paint);
            c.drawBitmap(p[6], left, top + p[4].getHeight(), paint);
            c.drawBitmap(p[7], left + p[4].getWidth(), top + p[4].getHeight(), paint);
        }
        int gridLeft = l[6], gridTop = l[8], boxW = l[10], boxH = l[11];
        Bitmap cursor = null;
        for (int i = 0; i < items.length; i++) {
            Bitmap b = items[i];
            if (b == null) continue;
            int page = d[i * N], body = d[i * N + 1], x = d[i * N + 2], y = d[i * N + 3], mode = d[i * N + 7];
            if (mode == IMODE_ONCURSOR) {
                cursor = b;
            } else if (page == 0) { // inventory grid
                c.drawBitmap(b, gridLeft + x * boxW, gridTop + y * boxH, paint);
            } else if (page == 255 && body > 0 && body < COMPONENT_OF_BODYLOC.length) {
                int k = 12 + COMPONENT_OF_BODYLOC[body] * 6;
                float cx = (l[k] + l[k + 1]) / 2f, cy = (l[k + 2] + l[k + 3]) / 2f;
                c.drawBitmap(b, cx - b.getWidth() / 2f, cy - b.getHeight() / 2f, paint);
            }
        }
        drawStore(c);
        // The item on the cursor follows the last touch, centred as D2Client draws it.
        if (cursor != null && !Float.isNaN(touchX)) c.drawBitmap(cursor, touchX - cursor.getWidth() / 2f, touchY - cursor.getHeight() / 2f, paint);
        c.restore();
    }

    private void drawStore(Canvas c) {
        Object[] st = storeState;
        Bitmap[] p = storePanel;
        if (st == null) return;
        int[] l = (int[]) st[0];
        Bitmap[] items = (Bitmap[]) st[1];
        int[] d = (int[]) st[2];
        if (p != null && p.length >= 4 && p[0] != null) { // four pieces, 2x2
            c.drawBitmap(p[0], l[0], l[2], paint);
            c.drawBitmap(p[1], l[0] + p[0].getWidth(), l[2], paint);
            c.drawBitmap(p[2], l[0], l[2] + p[0].getHeight(), paint);
            c.drawBitmap(p[3], l[0] + p[0].getWidth(), l[2] + p[0].getHeight(), paint);
        }
        int gridLeft = l[6], gridTop = l[8], boxW = l[10], boxH = l[11];
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null || d[i * N] != storePage) continue;
            c.drawBitmap(items[i], gridLeft + d[i * N + 2] * boxW, gridTop + d[i * N + 3] * boxH, paint);
        }
    }

    private static final int N = 8, IMODE_ONCURSOR = 4;
    /** Last touch in 800x600 coordinates. */
    private float touchX = Float.NaN, touchY = Float.NaN;
    /** A tap waiting for the native thread: {x, y} in 800x600, or null. */
    private volatile float[] pendingTap;

    @Override public boolean onTouchEvent(android.view.MotionEvent e) {
        Object[] s = state;
        if (s == null) return false;
        float scale = getHeight() / 600f;
        float x = (e.getX() - (getWidth() - 800 * scale) / 2f) / scale, y = e.getY() / scale;
        int[] l = (int[]) s[0];
        boolean inPanel = x >= l[0] && x < l[0] + 320 && y >= l[2] && y < l[2] + 502;
        Object[] st = storeState;
        if (st != null) { int[] sl = (int[]) st[0]; inPanel |= x >= sl[0] && x < sl[1] && y >= sl[2] && y < sl[3]; }
        boolean holding = hasCursorItem(s);
        if (!inPanel && !holding) return false; // the world and the stick get it
        touchX = x; touchY = y;
        if (e.getActionMasked() == android.view.MotionEvent.ACTION_UP) pendingTap = new float[]{x, y};
        invalidate();
        return true;
    }

    private static boolean hasCursorItem(Object[] s) {
        int[] d = (int[]) s[2];
        for (int i = 0; i + N - 1 < d.length; i += N) if (d[i + 7] == IMODE_ONCURSOR) return true;
        return false;
    }

    /**
     * Native thread: turns a pending tap into the packet the 1.10 client sends for that click. With
     * nothing on the cursor a tap takes the item under it (grid 0x19, body 0x1C); with an item on
     * the cursor it places it in the grid (0x18, top-left cell under the item's centre), equips or
     * swaps it on a body box (0x1A / 0x1D), or drops it outside the panel (0x17).
     */
    void handleTap() {
        float[] t = pendingTap;
        Object[] s = state;
        if (t == null || s == null) return;
        pendingTap = null;
        int[] l = (int[]) s[0], d = (int[]) s[2];
        int gridLeft = l[6], gridTop = l[8], boxW = l[10], boxH = l[11], cols = l[4], rows = l[5];
        int cursor = -1;
        for (int i = 0; i + N - 1 < d.length; i += N) if (d[i + 7] == IMODE_ONCURSOR) cursor = i;
        int body = bodyLocAt(l, t[0], t[1]);
        int v = vendor;
        Object[] st = storeState;
        if (v >= 0 && st != null && cursor < 0 && tradeTap(v, st, l, d, t)) return;
        if (cursor < 0) {
            for (int i = 0; i + N - 1 < d.length; i += N) {
                int page = d[i], loc = d[i + 1], x = d[i + 2], y = d[i + 3], w = d[i + 4], h = d[i + 5];
                if (page == 0 && t[0] >= gridLeft + x * boxW && t[0] < gridLeft + (x + w) * boxW
                        && t[1] >= gridTop + y * boxH && t[1] < gridTop + (y + h) * boxH) {
                    D2Native.itemCommand(0x19, d[i + 6], 0, 0, 0);
                    return;
                }
                if (page == 255 && loc > 0 && loc == body) { D2Native.itemCommand(0x1C, 0, loc, 0, 0); return; }
            }
            return;
        }
        int guid = d[cursor + 6], w = d[cursor + 4], h = d[cursor + 5];
        if (body > 0) {
            boolean occupied = false;
            for (int i = 0; i + N - 1 < d.length; i += N) occupied |= d[i] == 255 && d[i + 1] == body && d[i + 7] != IMODE_ONCURSOR;
            D2Native.itemCommand(occupied ? 0x1D : 0x1A, guid, body, 0, 0);
            return;
        }
        int gx = Math.round((t[0] - gridLeft) / boxW - w / 2f), gy = Math.round((t[1] - gridTop) / boxH - h / 2f);
        if (t[0] >= gridLeft && t[0] < gridLeft + cols * boxW && t[1] >= gridTop && t[1] < gridTop + rows * boxH) {
            gx = Math.max(0, Math.min(cols - w, gx)); gy = Math.max(0, Math.min(rows - h, gy));
            D2Native.itemCommand(0x18, guid, gx, gy, 0);
            return;
        }
        if (t[0] < l[0] || t[0] >= l[0] + 320) D2Native.itemCommand(0x17, guid, 0, 0, 0);
    }

    /** Vendor mode: a tap on a store item buys it, on the hero's grid item sells it, on the store's top changes page. */
    private boolean tradeTap(int v, Object[] st, int[] l, int[] d, float[] t) {
        int[] sl = (int[]) st[0], sd = (int[]) st[2];
        if (t[0] >= sl[0] && t[0] < sl[1] && t[1] >= sl[2] && t[1] < sl[3]) {
            int gl = sl[6], gt = sl[8], bw = sl[10], bh = sl[11];
            if (t[1] < gt) { // above the grid: next page that has items
                java.util.TreeSet<Integer> pages = new java.util.TreeSet<>();
                for (int i = 0; i + N - 1 < sd.length; i += N) pages.add(sd[i]);
                Integer next = pages.higher(storePage);
                storePage = next != null ? next : pages.isEmpty() ? -1 : pages.first();
                postInvalidate();
                return true;
            }
            for (int i = 0; i + N - 1 < sd.length; i += N) {
                if (sd[i] != storePage) continue;
                int x = sd[i + 2], y = sd[i + 3], w = sd[i + 4], h = sd[i + 5];
                if (t[0] >= gl + x * bw && t[0] < gl + (x + w) * bw && t[1] >= gt + y * bh && t[1] < gt + (y + h) * bh) {
                    int price = D2Native.itemPrice(v, sd[i + 6], 0);
                    if (price >= 0) D2Native.buy(v, sd[i + 6], price);
                    return true;
                }
            }
            return true;
        }
        int gridLeft = l[6], gridTop = l[8], boxW = l[10], boxH = l[11];
        for (int i = 0; i + N - 1 < d.length; i += N) {
            int x = d[i + 2], y = d[i + 3], w = d[i + 4], h = d[i + 5];
            if (d[i] == 0 && t[0] >= gridLeft + x * boxW && t[0] < gridLeft + (x + w) * boxW && t[1] >= gridTop + y * boxH && t[1] < gridTop + (y + h) * boxH) {
                int price = D2Native.itemPrice(v, d[i + 6], 1);
                if (price >= 0) D2Native.sell(v, d[i + 6], price);
                return true;
            }
        }
        return false;
    }

    /** Body location whose inventory.txt box contains the point, or 0. */
    private static int bodyLocAt(int[] l, float x, float y) {
        for (int loc = 1; loc < COMPONENT_OF_BODYLOC.length; loc++) {
            int k = 12 + COMPONENT_OF_BODYLOC[loc] * 6;
            if (x >= l[k] && x < l[k + 1] && y >= l[k + 2] && y < l[k + 3]) return loc;
        }
        return 0;
    }
}
