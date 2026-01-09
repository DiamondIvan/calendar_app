# Event Not Showing Issue - FIXED

## What Was Wrong

Your event wasn't showing on the calendar because:

1. **Event was created with userId = -1** (anonymous user)
2. **Calendar filters events by userId** - only shows events matching the logged-in user's ID
3. Since -1 ≠ 1 (or any real user ID), the event was invisible

## What Was Fixed

### 1. **Fixed Your Existing Event**

Changed the graduation event from:

```csv
1,-1,graduation,"my graduation",2026-01-25T01:15:00,2026-01-25T01:45:00,OTHER
```

To:

```csv
1,1,graduation,"my graduation",2026-01-25T01:15:00,2026-01-25T01:45:00,OTHER
```

Now it belongs to user ID 1 (user: a / abc@gmail.com)

### 2. **Updated CalendarPage.java**

- Now shows events for anonymous users (userId=-1) when not logged in
- Shows events for the logged-in user's ID when logged in
- No more "currentUser == null" early return blocking anonymous events

### 3. **Updated CreateEventPage.java**

- Added login requirement check at the start of `getView()`
- If user is not logged in, shows a message and "Go to Login" button
- Prevents creating events without proper authentication
- Ensures all new events have a valid userId

## How to Test

### Test 1: See Your Fixed Event

1. **Login** as user "a" (email: abc@gmail.com, password: abc)
2. Go to **Calendar** page
3. Navigate to **January 2026**
4. Your "graduation" event should now appear on **January 25, 2026**

### Test 2: Create a New Event

1. Make sure you're **logged in**
2. Go to **Create Event** page (should work now)
3. Fill in event details:
   - Title: "Test Event"
   - Start Date: Any date in January 2026
   - End Date: Same or later date
   - Category: Select any category
4. Click **CREATE EVENT**
5. Go back to **Calendar**
6. Your new event should appear!

### Test 3: Login Protection

1. **Logout** (go to home, clear session if there's a logout option)
2. Try to access **Create Event** page
3. You should see: "Please log in to create events" with a "Go to Login" button

## Your Current Users

From `users.csv`:

- **User 1**: Name: a, Email: abc@gmail.com, Password: abc
- **User 2**: Name: ivan, Email: fongjuntoh@gmail.com, Password: abc12345

## Expected Behavior Now

✅ Events show up after creation
✅ Events are linked to the logged-in user
✅ Can't create events without logging in
✅ Each user sees only their own events
✅ Anonymous users (if any) see events with userId=-1

## If Events Still Don't Show

**Quick Checklist:**

1. ✅ Are you logged in? Check console for "Login Successful: [name]"
2. ✅ Is the event saved? Check `backend/csvFiles/events.csv`
3. ✅ Does the userId match? Event userId should match your user ID
4. ✅ Is the date visible? Make sure you're viewing the correct month
5. ✅ Refresh/Reload the calendar page after creating an event

**Debug Steps:**

```powershell
# Check current events
Get-Content backend\csvFiles\events.csv

# Check current users
Get-Content backend\csvFiles\users.csv
```

## Data Format

Events CSV format:

```
id,userId,title,description,startDateTime,endDateTime,category
1,1,My Event,Description,2026-01-25T10:00:00,2026-01-25T11:00:00,PERSONAL
```

Important:

- **userId** must match a user ID from users.csv
- **Dates** must be in format: YYYY-MM-DDTHH:mm:ss
- **Category** must be one of: PROFESSIONAL, PERSONAL, HEALTH, EDUCATION, SOCIAL, FINANCE, HOLIDAY, OTHER

## Status: ✅ FIXED

Your calendar should now properly display events!
