# Event Editing and Deletion Guide

## ✨ New Features Added

You can now **edit** and **delete** events directly from the calendar!

### How to Edit or Delete an Event

#### Step 1: Click on an Event

- On the calendar, **click on any event label** (the colored boxes showing event names)
- A dialog will pop up showing event options

#### Step 2: Choose an Action

The dialog shows:

- **Event Details**: Start time, end time, and category
- **Three buttons**:
  - **Edit** - Modify the event details
  - **Delete** - Remove the event permanently
  - **Cancel** - Close the dialog without changes

### Editing an Event

1. Click on an event
2. Click the **Edit** button
3. A form appears with all current event details:
   - Title
   - Description
   - Start Date and Time
   - End Date and Time
   - Category
4. Make your changes
5. Click **Save** to update the event
6. The calendar refreshes automatically to show your changes

### Deleting an Event

1. Click on an event
2. Click the **Delete** button (red button)
3. A confirmation dialog appears asking if you're sure
4. Click **OK** to permanently delete
5. The event is removed from the calendar and CSV file

### Visual Cues

- **Event labels are now clickable** - cursor changes to a hand pointer when hovering
- **Delete button is red** - clearly marked for caution
- **Confirmation required for deletion** - prevents accidental deletions

## What Gets Updated

When you edit or delete an event:

- ✅ **CSV file is updated** - changes are saved to `backend/csvFiles/events.csv`
- ✅ **Calendar refreshes** - you see changes immediately
- ✅ **Event list reloads** - all data is synchronized

## Features Summary

### Edit Event Dialog Includes:

- ✏️ Title field
- ✏️ Description area
- 📅 Start date picker
- ⏰ Start time dropdown (15-minute intervals)
- 📅 End date picker
- ⏰ End time dropdown (15-minute intervals)
- 🏷️ Category selector

### Safety Features:

- ⚠️ Confirmation dialog before deletion
- ✅ Validation of all fields
- 🔄 Automatic calendar refresh after changes
- 💾 Immediate save to CSV

## Example Workflow

### Editing an Event:

```
1. See "Team Meeting" on calendar
2. Click on "Team Meeting" label
3. Dialog shows details
4. Click "Edit"
5. Change time from 10:00 to 14:00
6. Click "Save"
7. Event now shows at new time!
```

### Deleting an Event:

```
1. See "Old Event" on calendar
2. Click on "Old Event" label
3. Dialog shows details
4. Click "Delete" (red button)
5. Confirmation: "Are you sure?"
6. Click "OK"
7. Event disappears from calendar
```

## Important Notes

- 🔐 **You can only edit/delete your own events** - events are filtered by user
- 🎨 **Category colors update immediately** when changed
- 📝 **All fields are validated** before saving
- ⏱️ **Time slots are in 15-minute intervals** (00:00, 00:15, 00:30, 00:45, etc.)
- 🗑️ **Deletion is permanent** - deleted events cannot be recovered (unless you have a backup)

## Technical Details

### Methods Added to CalendarPage:

- `showEventOptionsDialog()` - Shows the options popup
- `editEvent()` - Opens edit form and handles updates
- `deleteEvent()` - Handles event deletion with confirmation
- `generateTimeSlots()` - Creates 15-minute interval time options
- `showAlert()` - Displays success/error messages

### Backend Integration:

Uses existing service methods:

- `eventService.updateEvent(id, event)` - Updates event in CSV
- `eventService.deleteEvent(id)` - Removes event from CSV
- `eventService.loadEvents()` - Reloads data after changes

## Testing Your Changes

1. **Start the application** (if not already running):

   ```powershell
   cd frontend
   mvn clean javafx:run
   ```

2. **Login** with your account

3. **Navigate to Calendar**

4. **Click on any existing event** - the options dialog should appear

5. **Try editing** - change the title or time

6. **Try deleting** - remove an old test event

7. **Verify changes** - check that the calendar updates correctly

## Troubleshooting

**Event click doesn't work?**

- Make sure you're clicking directly on the event label (colored text)
- Not on the empty space in the day cell

**Edit dialog shows wrong times?**

- Check that your event times are in valid format in the CSV
- Times should be HH:mm format (e.g., 14:30)

**Changes don't save?**

- Ensure you have write permissions to the CSV files
- Check console for error messages

**Deleted event still shows?**

- Try refreshing by navigating to a different month and back
- The calendar should auto-refresh, but manual refresh works too

## Status: ✅ READY TO USE

Event editing and deletion are now fully functional in your calendar application!
