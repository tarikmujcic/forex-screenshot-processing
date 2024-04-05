# forex-screenshot-processing

This is just a quick application that I've created for a client - which is used to help him create screenshots of his Forex analysis and add text with day and date to each column in the screenshot.

Application works as the following in the App.main method (in a loop):

    1. Simulates F12 key clicked 23 times
    2. Waits for Enter key
    3. Takes a screenshot
    4. Processes the screenshot by adding text for dates to it

Side note: the client was on a budget, so with time limitations, I couldn't take too much care in the CLEAN CODE, so forgive me, please. 

This is an example of the input screenshot taken and the output:

![source](https://github.com/MujcicTarik/forex-screenshot-processing/assets/93707322/63cf8b84-a2ca-414b-9e5c-93110f30100f)

This is the output:

![04-04To04-10-19](https://github.com/MujcicTarik/forex-screenshot-processing/assets/93707322/368c3fbf-aa9d-4cf7-9b83-6a7b42eab013)

Also, the Coordinates of the text can be edited using the 3 levels (1=top, 2=middle, 3=bottom):

![04-05To04-11-19](https://github.com/MujcicTarik/forex-screenshot-processing/assets/93707322/4ba6017c-6151-4fbc-bfee-bd3d1bde01ce)

