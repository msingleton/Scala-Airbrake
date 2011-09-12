# Scala Airbrake 
Scala client for airbrakeapp.com (formerly hoptoadapp.com)

It modifies the input elements by wrapping them in a container that holds the input element, as well as a span tag containing the placeholder text. If something is entered into the textbox, the placeholder text is hidden, if the text is removed the placeholder is restored.

## Usage
``` scala
AirbrakeNotifier.notify(request, exception)
```
