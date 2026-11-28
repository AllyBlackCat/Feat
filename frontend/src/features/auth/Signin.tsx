import { useState, type ChangeEvent, type SubmitEvent } from 'react';
import submitSignin from './authApi';
import './Signin.css';

function Signin() {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    function handleEmailChange(event: ChangeEvent<HTMLInputElement>) {
        setEmail(event.target.value);
    }

    function handlePasswordChange(event: ChangeEvent<HTMLInputElement>) {
        setPassword(event.target.value);
    }

    async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();
        try {
            const response = await submitSignin(email, password);

            if (!response.ok) {
                console.log('ログイン処理に失敗しました。')
                return;
            }

        } catch (error) {
            console.error(error);
        }
    }

    return (
        <main>
            <form onSubmit={handleSubmit}>
                <div className="signin-form">
                    <h1>ログイン</h1>
                    <label htmlFor="email">メールアドレス</label>
                    <input 
                        id="email" 
                        type="email" 
                        required
                        value={email}
                        onChange={handleEmailChange} />
                    <label htmlFor="password">パスワード</label>
                    <input 
                        id="password" 
                        type="password" 
                        required 
                        value={password}
                        onChange={handlePasswordChange} />
                    <button type="submit">ログイン</button>
                </div>
            </form>
        </main>
    );
}
export default Signin;