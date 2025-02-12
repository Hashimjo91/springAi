const sideList = document.querySelector('.vertical-side-list');
const containerTemplate = document.getElementById("container-template")

// Add event listener to each list item to track selection
Array.from(sideList.children).forEach((item) => {
    item.addEventListener('click', () => {
        document.querySelectorAll('.selected-item').forEach(li => {
            li.classList.remove('selected-item')
        })
        if (item.classList.contains('selected-item')) {
            item.classList.remove('selected-item');
        } else {
            item.classList.add('selected-item');
        }
    });
});
function getTime() {
    let d = new Date()
    let h = (d.getHours()<10?'0':'') + d.getHours(),
        m = (d.getMinutes()<10?'0':'') + d.getMinutes();
    return  h + ":" + m
}

// Add an event listener to the form submit event
document.getElementById("myForm").addEventListener("submit", function (event) {
    // Prevent default form submission behavior
    event.preventDefault();
    document.getElementById("chat_typing").classList.remove("hidden")
    const outputContainer = document.getElementById("messages")
    const newNode = containerTemplate.cloneNode(true).firstElementChild
    newNode.querySelector(".time").textContent = getTime()
    newNode.querySelector(".time").classList.add("time-left")
    let element = document.querySelector('.selected-item');
    let liValue = element?.getAttribute("value")
    // Get the value of the input field
    var inputText = document.getElementById("input").value;
    newNode.querySelector(".container-text").textContent = inputText
    outputContainer.append(newNode)
    var objDiv = document.getElementById('output');
    objDiv.scrollTop = objDiv.scrollHeight
    // Create a new request object
    var req = new XMLHttpRequest();

    // Set the request method and URL
    req.open("GET", "http://localhost:8080/ai?message=" + inputText + (liValue ? ("&doi=" + encodeURI(liValue)) : ""), true);

    // Add a listener for the response data event
    req.onload = function () {
        if (req.status === 200) {
            document.getElementById("chat_typing").classList.add("hidden")
            // Get the response text as JSON
            const newNodeRes = containerTemplate.cloneNode(true).firstElementChild
            var respJson = JSON.parse(req.responseText);
            newNodeRes.classList.add("darker")
            newNodeRes.querySelector(".container-text").textContent = respJson.aiResponse
            newNodeRes.querySelector(".time").textContent = getTime()
            newNodeRes.querySelector(".time").classList.add("time-right")
            newNodeRes.querySelector("img").classList.add("right")
            newNodeRes.querySelector("img").src = "/images/robot.png"
            outputContainer.append(newNodeRes)

        } else {
            // Display an error message if the request fails
            document.getElementById("chat_typing").classList.add("hidden")
            console.log( "Error: Request failed with status code " + req.status)
        }
        var objDiv = document.getElementById('output');
        objDiv.scrollTop = objDiv.scrollHeight
    };

    // Add a listener for the error event
    req.onerror = function () {
        // Display an error message if the request fails
        document.getElementById("chat_typing").classList.add("hidden")
        console.log( "Error: Request failed with status code " + req.status)
    };

    // Send the request
    req.send();
});