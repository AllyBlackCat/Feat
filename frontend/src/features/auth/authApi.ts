async function submitSignin (email: string, password: string) {
    const response = await fetch('/api/auth/signin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'same-origin',
            body: JSON.stringify({
                email, 
                password
            })
        }
    );

    return response;
}

export default submitSignin;