import { useState, type ChangeEvent, type SubmitEvent } from 'react';
import { submitSignin, getMe } from './authApi';
import './Signin.css';

function Signin() {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    function handleEmailChange(event: ChangeEvent<HTMLInputElement>) {
        setEmail(event.target.value);
    }

    function handlePasswordChange(event: ChangeEvent<HTMLInputElement>) {
        setPassword(event.target.value);
    }

    async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();
        setErrorMessage(''); // 毎回エラーメッセージを更新するため
        try {
            const response = await submitSignin(email, password);

            if (!response.ok) {
                setErrorMessage('ログインに失敗しました。');
                return;
            }
            const meResponse = await getMe();

            if (!meResponse.ok) {
                setErrorMessage('ユーザー情報の取得に失敗しました');
                return;
            }
            const user = await meResponse.json();
            console.log(user);

        } catch (error) {
            console.error(error);
            setErrorMessage('通信エラーが発生しました。');
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
                    {errorMessage && (
                        <p role="alert">{errorMessage}</p>
                    )}
                </div>
            </form>
        </main>
    );
}
export default Signin;