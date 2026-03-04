# NoCloudChat MVP — Test Plan

**Version**: 1.0 | **Date**: 2026-03-02 | **Author**: PM

---

## 1. Test Environment

- **Minimum**: 2 NoCloudChat instances running simultaneously
  - Option A: 2 machines on the same Wi-Fi/LAN
  - Option B: 2 terminal instances on the same machine (localhost UDP broadcast works)
- **Platforms**: Windows, macOS, Linux (at least 2 must pass)

---

## 2. Feature: Peer Discovery

### TC-01: Single peer appears
1. Start Instance A
2. Start Instance B on same network
3. **Expected**: Within 5 seconds, Instance A shows Instance B in the peer list (and vice versa)
4. **Expected**: Peer shows correct display name (OS username by default)
5. **Expected**: Peer shows correct IP address

### TC-02: Multiple peers
1. Start 3 instances (A, B, C)
2. **Expected**: Each instance shows the other 2 in the peer list
3. **Expected**: Peer count badge shows correct number (2)

### TC-03: Peer goes offline
1. Start Instance A and B (both visible to each other)
2. Close Instance B
3. Wait 15 seconds
4. **Expected**: Instance B disappears from A's peer list
5. **Expected**: Peer count badge updates

### TC-04: Peer restarts
1. Start A and B (connected)
2. Close B and reopen it
3. **Expected**: B re-appears in A's list within 5 seconds with fresh display name

---

## 3. Feature: Text Messaging

### TC-05: Send message
1. Open A and B (A sees B in peer list)
2. On A: click on B → type "Hello" → press Enter
3. **Expected**: "Hello" appears in A's chat view immediately
4. **Expected**: "Hello" appears in B's chat view immediately
5. **Expected**: Message shows timestamp and sender name on B's side

### TC-06: Receive message
1. On B: send "Hi back" to A
2. **Expected**: "Hi back" appears in A's chat view
3. **Expected**: Message bubble alignment: A's messages right (red), B's messages left (blue)

### TC-07: Multi-line message
1. Type a message and press Shift+Enter
2. **Expected**: New line added to input (message not sent)
3. Press Enter
4. **Expected**: Multi-line message sent as a single bubble

### TC-08: Empty message not sent
1. Click send button with empty input
2. **Expected**: No message sent, no error shown

### TC-09: Message history preserved during session
1. Send several messages between A and B
2. Click on a different peer (C)
3. Click back on B
4. **Expected**: Previous message history is shown intact

---

## 4. Feature: Unread Notifications

### TC-10: Unread badge
1. A and B are both running
2. A has a chat with C open (not B)
3. B sends A a message
4. **Expected**: B's entry in A's peer list shows a red badge with count "1"
5. Click on B in A's peer list
6. **Expected**: Badge disappears

### TC-11: Toast notification
1. A has chat with C open
2. B sends A a message
3. **Expected**: Toast appears bottom-right with B's name and message preview
4. **Expected**: Toast disappears after ~4 seconds

---

## 5. Feature: Display Name Settings

### TC-12: Change name
1. Click the avatar/name area (top-left of sidebar)
2. Settings panel opens with current name pre-filled
3. Change name to "Tester123"
4. Click Save
5. **Expected**: Sidebar shows "Tester123" immediately
6. Within 3-5 seconds, other instances update their peer list to show "Tester123"

### TC-13: Cancel settings
1. Open settings, change name to something else
2. Press Escape (or click Cancel)
3. **Expected**: Name reverts to original in sidebar

### TC-14: Empty name rejected
1. Open settings, clear the name field
2. Click Save
3. **Expected**: Name does not change (original preserved), panel stays open

---

## 6. Non-Functional Tests

### TC-15: Launch time
1. Open task manager / Activity Monitor
2. Launch NoCloudChat
3. **Expected**: Window visible within 2 seconds

### TC-16: Discovery time
1. Start Instance A (no peers)
2. Start Instance B
3. Measure time until B appears in A's peer list
4. **Expected**: < 5 seconds

### TC-17: Memory usage
1. Run app with 3 peers, exchange 100+ messages
2. Check memory usage
3. **Expected**: < 200 MB RSS

---

## 7. Acceptance Criteria Summary

| Test | Pass Criteria |
|---|---|
| TC-01 | Peer discovered in < 5s |
| TC-02 | All 3 peers visible |
| TC-03 | Peer removed within 15s |
| TC-05 | Message visible on both sides |
| TC-06 | Correct bubble alignment |
| TC-10 | Unread badge shown/cleared |
| TC-12 | Name updates propagated |
| TC-15 | App starts in < 2s |

