function isEquilibrium(arr) {
    const totalSum = arr.reduce((acc, val) => acc + val, 0);
    let leftSum = 0;

    for (let i = 0; i < arr.length; i++) {
        const currentElement = arr[i];
        const rightSum = totalSum - leftSum - currentElement;

        if (leftSum === rightSum) {
            return "YES";
        }

        leftSum += currentElement;
    }

    return "NO";
}

// Example usage:

// Example 1:
console.log(isEquilibrium([1, 2, 3])); // Output: NO

// Example 2:
console.log(isEquilibrium([1, 2, 4, 2, 1])); // Output: YES

// Example 3:
console.log(isEquilibrium([6, 0, 0, 0])); // Output: YES

// Additional test case:
console.log(isEquilibrium([7, 3, 8, 10])); // Output: YES
