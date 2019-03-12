function randomVal(max, min, idToUpdate) {
    var newVal = Math.random() * (max - min) + min;
    console.log("New Value has been henerated : " + String(newVal.toFixed(2)));

     gauge1 = Gauge(
         document.getElementById(String(idToUpdate))
    );
    update(newVal);
}

function update(int){
    gauge1.setValue(int);
}

function randomValue(min,max){
    var newValue = Math.random() * (max - min) + min;
    return newValue.toFixed(2);
}